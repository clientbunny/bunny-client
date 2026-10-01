package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;

public class ZoomFeature {
    private static boolean zooming = false;
    private static float currentFactor = 1.0f;
    private static final float TARGET_FACTOR = 0.25f; // 4x zoom

    public static void setZooming(boolean isZooming) {
        zooming = isZooming;
    }

    public static boolean isZooming() {
        return zooming && ConfigManager.getConfig().zoomEnabled;
    }

    public static double modifyFov(double originalFov) {
        if (!ConfigManager.getConfig().zoomEnabled) {
            return originalFov;
        }

        if (ConfigManager.getConfig().smoothZoom) {
            float target = zooming ? TARGET_FACTOR : 1.0f;
            currentFactor += (target - currentFactor) * 0.25f;
            return originalFov * currentFactor;
        } else {
            return zooming ? (originalFov * TARGET_FACTOR) : originalFov;
        }
    }
}
