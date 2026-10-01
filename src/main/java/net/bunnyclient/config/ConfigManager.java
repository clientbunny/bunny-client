package net.bunnyclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

/**
 * Manages JSON persistence of Bunny Client user settings.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BunnyConfig config;
    private static File configFile;

    public static BunnyConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }

    private static File getConfigFile() {
        if (configFile == null) {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            configFile = configDir.resolve("bunnyclient.json").toFile();
        }
        return configFile;
    }

    public static void load() {
        File file = getConfigFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, BunnyConfig.class);
            } catch (Exception e) {
                System.err.println("[BunnyClient] Failed to load config, resetting to default: " + e.getMessage());
                config = new BunnyConfig();
            }
        } else {
            config = new BunnyConfig();
            save();
        }

        if (config == null) {
            config = new BunnyConfig();
        }
        config.initDefaults();
    }

    public static void save() {
        if (config == null) {
            config = new BunnyConfig();
        }
        File file = getConfigFile();
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            System.err.println("[BunnyClient] Failed to save config: " + e.getMessage());
        }
    }
}
