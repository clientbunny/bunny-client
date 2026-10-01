package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.util.Identifier;

/**
 * Since 1.21.9 hitboxes are no longer drawn by EntityRenderManager; they are a debug-HUD entry
 * (the same one F3+B toggles). We just keep that entry in sync with our config value.
 */
public class HitboxManager {
    // Same id the vanilla debug profile uses for F3+B. Looked up by id because Yarn leaves the constant unnamed.
    private static final Identifier ENTITY_HITBOXES = Identifier.ofVanilla("entity_hitboxes");

    public static boolean isEnabled() {
        return ConfigManager.getConfig().hitboxes;
    }

    public static void setEnabled(boolean enabled) {
        ConfigManager.getConfig().hitboxes = enabled;
        ConfigManager.save();
        sync(MinecraftClient.getInstance());
    }

    public static void toggle() {
        setEnabled(!isEnabled());
    }

    /** Called every client tick; cheap no-op when already in sync. */
    public static void sync(MinecraftClient client) {
        if (client == null) return;
        DebugHudProfile profile = client.debugHudEntryList;
        if (profile == null) return;
        boolean visible = profile.isEntryVisible(ENTITY_HITBOXES);
        if (visible != isEnabled()) {
            profile.toggleVisibility(ENTITY_HITBOXES);
        }
    }
}
