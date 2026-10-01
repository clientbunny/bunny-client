package net.bunnyclient.hud.modules;

import net.bunnyclient.features.ToggleSprintFeature;
import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class ToggleSprintHudModule extends HudModule {

    public ToggleSprintHudModule() {
        super("sprint_status", "Sprint / Sneak Status", "Shows when sprint or sneak is actively toggled", 6, 48, 88, 14, true);
        this.showBackground = false;
        this.showBorder = false;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        boolean sprinting = ToggleSprintFeature.isSprintToggled();
        boolean sneaking = ToggleSprintFeature.isSneakToggled();

        if (!sprinting && !sneaking) return;

        String text = "";
        if (sprinting && sneaking) {
            text = "[Sprint + Sneak (Toggled)]";
        } else if (sprinting) {
            text = "[Sprinting (Toggled)]";
        } else {
            text = "[Sneaking (Toggled)]";
        }

        int textW = client.textRenderer.getWidth(text);
        this.width = textW + 8;
        this.height = 14;

        if (showBackground) {
            renderBackground(context, width, height);
        }

        context.drawText(client.textRenderer, text, x + (showBackground ? 4 : 0), y + 3, ColorUtil.PRIMARY, true);
    }

    @Override
    public void renderEditor(DrawContext context) {
        String text = "[Sprinting (Toggled)]";
        int textW = client.textRenderer.getWidth(text);
        this.width = textW + 8;
        this.height = 14;
        renderBackground(context, width, height);
        context.drawText(client.textRenderer, text, x + 4, y + 3, ColorUtil.PRIMARY, true);
    }
}
