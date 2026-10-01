package net.bunnyclient.util;

import com.google.gson.Gson;
import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows which players are running Bunny Client.
 *  - The local player always counts.
 *  - Other players are learned from the optional backend (config.backendUrl):
 *      POST {url}/heartbeat   {"uuid": "..."}     (we announce ourselves)
 *      GET  {url}/users                           (JSON array of UUID strings)
 */
public final class BunnyUsers {
    private BunnyUsers() {}

    private static final Logger LOG = LoggerFactory.getLogger("BunnyClient/Badge");
    private static final Set<UUID> USERS = ConcurrentHashMap.newKeySet();
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private static volatile boolean running = false;

    public static boolean isBunnyUser(UUID uuid) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getUuid().equals(uuid)) return true;
        return USERS.contains(uuid);
    }

    public static void start() {
        if (running) return;
        running = true;
        Thread t = new Thread(BunnyUsers::loop, "BunnyClient-Users");
        t.setDaemon(true);
        t.start();
    }

    private static void loop() {
        boolean loggedWaiting = false;
        while (running) {
            String base = ConfigManager.getConfig().backendUrl;
            MinecraftClient mc = MinecraftClient.getInstance();
            try {
                if (base == null || base.isBlank()) {
                    USERS.clear();
                } else if (mc.player == null) {
                    if (!loggedWaiting) {
                        LOG.info("Badge network ready ({}), waiting until you join a world...", base);
                        loggedWaiting = true;
                    }
                } else {
                    loggedWaiting = false;
                    base = base.trim().replaceAll("/+$", "");
                    String uuid = mc.player.getUuid().toString();

                    HttpResponse<String> hb = HTTP.send(HttpRequest.newBuilder(URI.create(base + "/heartbeat"))
                            .timeout(Duration.ofSeconds(60))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{\"uuid\":\"" + uuid + "\"}")).build(),
                            HttpResponse.BodyHandlers.ofString());
                    LOG.info("Heartbeat {} -> HTTP {}", uuid, hb.statusCode());

                    HttpResponse<String> res = HTTP.send(HttpRequest.newBuilder(URI.create(base + "/users"))
                            .timeout(Duration.ofSeconds(60)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() == 200) {
                        String[] ids = GSON.fromJson(res.body(), String[].class);
                        Set<UUID> fresh = ConcurrentHashMap.newKeySet();
                        if (ids != null) for (String id : ids) {
                            try { fresh.add(UUID.fromString(id)); } catch (IllegalArgumentException ignored) {}
                        }
                        USERS.retainAll(fresh);
                        USERS.addAll(fresh);
                        LOG.info("Badge users online: {}", fresh.size());
                    } else {
                        LOG.warn("GET /users returned HTTP {}", res.statusCode());
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                // keep last known list, but tell the user why
                LOG.warn("Badge network request failed ({}): {}", base, e.toString());
            }
            try { Thread.sleep(20_000); } catch (InterruptedException e) { return; }
        }
    }

    public static Set<UUID> snapshot() { return Collections.unmodifiableSet(USERS); }
}
