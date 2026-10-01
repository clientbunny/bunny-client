package net.bunnyclient.hud;

import net.bunnyclient.hud.modules.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HudManager {
    private static final HudManager INSTANCE = new HudManager();
    private final List<HudModule> modules = new ArrayList<>();

    public static HudManager getInstance() {
        return INSTANCE;
    }

    private HudManager() {
        // Register all built-in modules
        register(new FpsHudModule());
        register(new CoordinatesHudModule());
        register(new ArmorStatusHudModule());
        register(new PotionStatusHudModule());
        register(new KeystrokesHudModule());
        register(new CpsHudModule());
        register(new PingHudModule());
        register(new ToggleSprintHudModule());
    }

    public void register(HudModule module) {
        modules.add(module);
    }

    public List<HudModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public HudModule getModule(String id) {
        for (HudModule m : modules) {
            if (m.getId().equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Renders all active HUD modules.
     */
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        for (HudModule module : modules) {
            if (module.isEnabled()) {
                module.render(context, tickCounter);
            }
        }
    }

    /**
     * Resets all HUD module positions to defaults.
     */
    public void resetPositions() {
        for (HudModule m : modules) {
            m.syncFromConfig();
        }
    }
}
