package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class FpsHudModule extends HudModule {

    public FpsHudModule() {
        super("fps", "FPS Counter", "Displays real-time frames per second", 6, 6, 58, 16, true);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        int fps = client.getCurrentFps();
        String fpsNum = String.valueOf(fps);
        String fpsLabel = " FPS";

        int numWidth = client.textRenderer.getWidth(fpsNum);
        int labelWidth = client.textRenderer.getWidth(fpsLabel);
        this.width = Math.max(54, numWidth + labelWidth + 12);
        this.height = 16;

        renderBackground(context, width, height);

        int textY = y + 4;
        context.drawText(client.textRenderer, fpsNum, x + 6, textY, ColorUtil.PRIMARY, true);
        context.drawText(client.textRenderer, fpsLabel, x + 6 + numWidth, textY, ColorUtil.SECONDARY, true);
    }
}
