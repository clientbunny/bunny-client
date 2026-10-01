package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

public class CpsHudModule extends HudModule {

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean lastLeftPressed = false;
    private boolean lastRightPressed = false;

    public CpsHudModule() {
        super("cps", "CPS Counter", "Displays left and right clicks per second", 70, 6, 60, 16, true);
    }

    private void updateClicks() {
        long now = System.currentTimeMillis();
        boolean left = client.options.attackKey.isPressed();
        boolean right = client.options.useKey.isPressed();

        if (left && !lastLeftPressed) {
            leftClicks.add(now);
        }
        if (right && !lastRightPressed) {
            rightClicks.add(now);
        }

        lastLeftPressed = left;
        lastRightPressed = right;

        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        updateClicks();

        String text = leftClicks.size() + " | " + rightClicks.size() + " CPS";
        int textW = client.textRenderer.getWidth(text);
        this.width = Math.max(56, textW + 12);
        this.height = 16;

        renderBackground(context, width, height);

        int textY = y + 4;
        context.drawText(client.textRenderer, text, x + 6, textY, ColorUtil.PRIMARY, true);
    }
}
