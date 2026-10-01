package net.bunnyclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public final class BunnySoundUtil {
    private BunnySoundUtil() {}

    private static long lastSliderSoundTime = 0;

    public static void playClick() {
        playSound(1.20f);
    }

    public static void playTabSwitch() {
        playSound(1.10f);
    }

    public static void playToggle(boolean enabled) {
        playSound(enabled ? 1.35f : 0.95f);
    }

    public static void playSliderTick() {
        long now = System.currentTimeMillis();
        if (now - lastSliderSoundTime > 60) {
            lastSliderSoundTime = now;
            playSound(1.40f);
        }
    }

    private static void playSound(float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getSoundManager() != null) {
            client.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, pitch)
            );
        }
    }
}