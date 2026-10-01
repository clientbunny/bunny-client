package net.bunnyclient.discord;

import com.google.gson.JsonObject;
import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Pure Java Discord IPC client.
 * Connects directly to Discord's local IPC named pipe / socket with zero native DLL dependencies.
 * Asynchronous, daemon-based, completely safe and cross-platform.
 */
public class DiscordRpcManager {
    private static final DiscordRpcManager INSTANCE = new DiscordRpcManager();
    private static final String CLIENT_ID = "1553772108616827010"; // Bunny Client Discord App ID
    private static final long START_TIME = System.currentTimeMillis() / 1000L;

    private Thread workerThread;
    private volatile boolean running = false;
    private RandomAccessFile ipcPipe = null;

    public static DiscordRpcManager getInstance() {
        return INSTANCE;
    }

    private DiscordRpcManager() {}

    public synchronized void start() {
        if (running) return;
        running = true;

        workerThread = new Thread(this::runLoop, "BunnyClient-DiscordRPC");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public synchronized void stop() {
        running = false;
        closePipe();
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
    }

    private void runLoop() {
        while (running) {
            try {
                if (ConfigManager.getConfig().discordRpc) {
                    if (ipcPipe == null) {
                        connect();
                    }
                    if (ipcPipe != null) {
                        updatePresence();
                    }
                } else if (ipcPipe != null) {
                    closePipe();
                }

                // Sleep between updates
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                closePipe();
                try {
                    Thread.sleep(15000); // Retry after 15 seconds if Discord closed
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    private void connect() {
        for (int i = 0; i < 10; i++) {
            try {
                String pipePath = System.getProperty("os.name").toLowerCase().contains("win")
                        ? "\\\\.\\pipe\\discord-ipc-" + i
                        : getUnixPipePath(i);

                File file = new File(pipePath);
                if (file.exists() || System.getProperty("os.name").toLowerCase().contains("win")) {
                    ipcPipe = new RandomAccessFile(pipePath, "rw");

                    // Handshake packet (Opcode 0)
                    JsonObject handshake = new JsonObject();
                    handshake.addProperty("v", 1);
                    handshake.addProperty("client_id", CLIENT_ID);

                    writePacket(0, handshake.toString());
                    readPacket(); // Read response ack
                    return;
                }
            } catch (Exception ignored) {
                closePipe();
            }
        }
    }

    private String getUnixPipePath(int index) {
        String xdg = System.getenv("XDG_RUNTIME_DIR");
        if (xdg != null && !xdg.isEmpty()) {
            return xdg + "/discord-ipc-" + index;
        }
        String tmp = System.getenv("TMPDIR");
        if (tmp == null || tmp.isEmpty()) {
            tmp = "/tmp";
        }
        return tmp + "/discord-ipc-" + index;
    }

    private void updatePresence() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        String details = "In Main Menu";
        String state = "Bunny Client 1.21.11";

        if (client.player != null) {
            if (client.isInSingleplayer()) {
                details = "Singleplayer";
                state = "Exploring the world";
            } else {
                ServerInfo server = client.getCurrentServerEntry();
                if (server != null && ConfigManager.getConfig().discordShowServer) {
                    details = "Multiplayer: " + server.address;
                } else {
                    details = "Playing Multiplayer";
                }
                state = "FPS: " + client.getCurrentFps();
            }
        }

        JsonObject activity = new JsonObject();
        activity.addProperty("details", details);
        activity.addProperty("state", state);

        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", START_TIME);
        activity.add("timestamps", timestamps);

        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", "aloclientlogo");
        assets.addProperty("large_text", "Bunny Client v" + net.bunnyclient.BunnyClient.VERSION);
        assets.addProperty("small_image", "minecraft");
        assets.addProperty("small_text", "Minecraft 1.21.11 (Vulkan)");
        activity.add("assets", assets);

        JsonObject args = new JsonObject();
        args.addProperty("pid", (int) ProcessHandle.current().pid());
        args.add("activity", activity);

        JsonObject packet = new JsonObject();
        packet.addProperty("cmd", "SET_ACTIVITY");
        packet.add("args", args);
        packet.addProperty("nonce", UUID.randomUUID().toString());

        try {
            writePacket(1, packet.toString());
            readPacket();
        } catch (Exception e) {
            closePipe();
        }
    }

    private void writePacket(int opcode, String json) throws Exception {
        if (ipcPipe == null) return;
        byte[] bytes = json.getBytes("UTF-8");
        ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(opcode);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        ipcPipe.write(buffer.array());
    }

    private void readPacket() throws Exception {
        if (ipcPipe == null) return;
        byte[] header = new byte[8];
        ipcPipe.readFully(header);
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int opcode = buffer.getInt();
        int length = buffer.getInt();
        if (length > 0 && length < 65536) {
            byte[] body = new byte[length];
            ipcPipe.readFully(body);
        }
    }

    private void closePipe() {
        if (ipcPipe != null) {
            try {
                ipcPipe.close();
            } catch (Exception ignored) {}
            ipcPipe = null;
        }
    }
}
