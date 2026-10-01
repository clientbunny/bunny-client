package net.bunnyclient;

import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.discord.DiscordRpcManager;
import net.bunnyclient.features.*;
import net.bunnyclient.gui.BunnyMenuScreen;
import net.bunnyclient.hud.HudManager;
import net.bunnyclient.util.BunnyUsers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier; // Added
import org.lwjgl.glfw.GLFW;

public class BunnyClient implements ClientModInitializer {
    public static final String MOD_ID = "bunnyclient";

    /** Read from gradle.properties (mod_version) via fabric.mod.json - change it there only. */
    public static final String VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");

    public static KeyBinding openMenuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding toggleSprintKey;
    public static KeyBinding toggleSneakKey;
    public static KeyBinding toggleHitboxesKey;
    public static KeyBinding toggleFullbrightKey;

    @Override
    public void onInitializeClient() {
        System.out.println("[BunnyClient] Initializing Bunny Client 1.21.11 (Vulkan Compatible)...");

        ConfigManager.load();

        registerKeybindings();

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            HudManager.getInstance().render(drawContext, tickCounter);
        });

        // Runs before vanilla reads input, so forced sprint/sneak keys take effect this tick
        ClientTickEvents.START_CLIENT_TICK.register(ToggleSprintFeature::tick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HitboxManager.sync(client);
            if (client.player == null) return;

            while (openMenuKey.wasPressed()) {
                client.setScreen(new BunnyMenuScreen());
            }

            ZoomFeature.setZooming(zoomKey.isPressed());

            while (toggleSprintKey.wasPressed()) {
                ToggleSprintFeature.toggleSprint();
            }

            while (toggleSneakKey.wasPressed()) {
                ToggleSprintFeature.toggleSneak();
            }

            while (toggleHitboxesKey.wasPressed()) {
                HitboxManager.toggle();
            }

            while (toggleFullbrightKey.wasPressed()) {
                FullbrightFeature.toggle();
            }
        });

        BunnyUsers.start();
        DiscordRpcManager.getInstance().start();

        System.out.println("[BunnyClient] Bunny Client initialized successfully!");
    }

    private void registerKeybindings() {
        // Create the category once
        KeyBinding.Category CATEGORY = KeyBinding.Category.create(
            Identifier.of(MOD_ID, "general")
        );

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            CATEGORY // Changed
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY // Changed
        ));

        toggleSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.toggle_sprint",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY // Changed
        ));

        toggleSneakKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.toggle_sneak",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY // Changed
        ));

        toggleHitboxesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.toggle_hitboxes",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY // Changed
        ));

        toggleFullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bunnyclient.toggle_fullbright",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY // Changed
        ));
    }
}