package net.bunnyclient.features;

import net.bunnyclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

/**
 * Toggle sprint / toggle sneak implemented by driving the vanilla key bindings.
 * This avoids touching Input internals, which changed heavily in 1.21.x.
 *
 * Menu switch  = feature available.   Keybind = switch the held state on/off.
 */
public class ToggleSprintFeature {
    private static boolean sprintToggled = true;   // auto-sprint is on by default when the feature is enabled
    private static boolean sneakToggled = false;
    private static boolean sprintForced = false;
    private static boolean sneakForced = false;

    public static void toggleSprint() { sprintToggled = !sprintToggled; }
    public static void toggleSneak()  { sneakToggled = !sneakToggled; }

    public static boolean isSprintToggled() {
        return ConfigManager.getConfig().toggleSprint && sprintToggled;
    }

    public static boolean isSneakToggled() {
        return ConfigManager.getConfig().toggleSneak && sneakToggled;
    }

    public static void setSprintToggled(boolean toggled) { sprintToggled = toggled; }
    public static void setSneakToggled(boolean toggled)  { sneakToggled = toggled; }

    /** Call at START_CLIENT_TICK so the key state is in place before vanilla reads input. */
    public static void tick(MinecraftClient client) {
        GameOptions o = client.options;
        boolean active = client.player != null && client.currentScreen == null;

        if (active && isSneakToggled()) {
            o.sneakKey.setPressed(true);
            sneakForced = true;
        } else if (sneakForced) {
            o.sneakKey.setPressed(false);
            sneakForced = false;
        }

        if (active && isSprintToggled() && !isSneakToggled() && o.forwardKey.isPressed()) {
            o.sprintKey.setPressed(true);
            sprintForced = true;
        } else if (sprintForced) {
            o.sprintKey.setPressed(false);
            sprintForced = false;
        }
    }
}
