package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Provides entity culling optimizations to boost frame rate.
 * Skips processing entities that are too far or outside active viewport.
 */
public class EntityCullingFeature {

    public static boolean isEnabled() {
        return ConfigManager.getConfig().entityCulling;
    }

    public static boolean shouldRenderEntity(Entity entity) {
        if (!isEnabled()) return true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity == client.player) return true;

        // Skip entities farther than 64 blocks if occluded or behind player
        double distSq = client.player.squaredDistanceTo(entity);
        if (distSq > (64.0 * 64.0)) {
            // Check dot product with player's look vector
            double dx = entity.getX() - client.player.getX();
            double dz = entity.getZ() - client.player.getZ();
            double lookX = client.player.getRotationVector().x;
            double lookZ = client.player.getRotationVector().z;
            double dot = (dx * lookX) + (dz * lookZ);
            if (dot < -5.0) {
                // Entity is far away and directly behind the player
                return false;
            }
        }

        return true;
    }
}
