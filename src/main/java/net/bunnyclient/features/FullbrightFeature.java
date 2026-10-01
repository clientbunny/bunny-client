package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;

public class FullbrightFeature {

    public static boolean isEnabled() {
        return ConfigManager.getConfig().fullbright;
    }

    public static void toggle() {
        ConfigManager.getConfig().fullbright = !ConfigManager.getConfig().fullbright;
        ConfigManager.save();
    }

    public static void setEnabled(boolean enabled) {
        ConfigManager.getConfig().fullbright = enabled;
        ConfigManager.save();
    }
}
