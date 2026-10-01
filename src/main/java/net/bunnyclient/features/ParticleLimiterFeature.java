package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;

public class ParticleLimiterFeature {
    private static int particleCounter = 0;

    public static boolean isEnabled() {
        return ConfigManager.getConfig().particleLimiter;
    }

    public static boolean shouldSpawnParticle() {
        if (!isEnabled()) return true;

        particleCounter++;
        // Keep 1 out of every 2 particles during high particle stress
        return (particleCounter % 2) == 0;
    }
}
