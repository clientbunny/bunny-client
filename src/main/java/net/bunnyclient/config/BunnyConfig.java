package net.bunnyclient.config;

import net.bunnyclient.util.ColorUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration data holder for Bunny Client.
 */
public class BunnyConfig {

    public static class ModulePos {
        public int x;
        public int y;
        public boolean enabled;
        public boolean background;
        public boolean border;

        public ModulePos() {}

        public ModulePos(int x, int y, boolean enabled, boolean background, boolean border) {
            this.x = x;
            this.y = y;
            this.enabled = enabled;
            this.background = background;
            this.border = border;
        }
    }

    // Positions & toggles for HUD modules
    public Map<String, ModulePos> modules = new HashMap<>();

    // QOL Settings
    public boolean toggleSprint = true;
    public boolean toggleSneak = false;
    public boolean fullbright = false;
    public boolean zoomEnabled = true;
    public float zoomFov = 25.0f;
    public boolean smoothZoom = true;
    public boolean hitboxes = false;
    public boolean customCrosshair = false;
    public int crosshairType = 0; // 0=vanilla, 1=dot, 2=cross, 3=circle

    // Performance
    public boolean entityCulling = true;
    public boolean particleLimiter = false;

    // Cosmetics
    public boolean bunnyBadge = true;
    // Base URL of the Bunny backend (see /backend). Empty = badge only on yourself.
    public String backendUrl = "https://bunnybadgeprivate.onrender.com";
    public boolean lunarTitleScreen = true;

    // Discord Rich Presence
    public boolean discordRpc = true;
    public boolean discordShowServer = true;

    // Color customization
    public int primaryColor = ColorUtil.PRIMARY;
    public int secondaryColor = ColorUtil.SECONDARY;

    public BunnyConfig() {
        initDefaults();
    }

    public void initDefaults() {
        // Default positions suited for standard 1080p / GUI scale 2-3
        modules.putIfAbsent("fps", new ModulePos(6, 6, true, true, true));
        modules.putIfAbsent("coords", new ModulePos(6, 26, true, true, true));
        modules.putIfAbsent("armor", new ModulePos(6, 75, true, true, true));
        modules.putIfAbsent("potions", new ModulePos(6, 175, true, true, true));
        modules.putIfAbsent("keystrokes", new ModulePos(6, 245, false, true, true));
        modules.putIfAbsent("cps", new ModulePos(100, 6, true, true, true));
        modules.putIfAbsent("ping", new ModulePos(165, 6, true, true, true));
        modules.putIfAbsent("sprint_status", new ModulePos(6, 48, true, false, false));
    }

    public ModulePos getModulePos(String id, int defaultX, int defaultY, boolean defaultEnabled) {
        if (!modules.containsKey(id)) {
            modules.put(id, new ModulePos(defaultX, defaultY, defaultEnabled, true, true));
        }
        return modules.get(id);
    }
}
