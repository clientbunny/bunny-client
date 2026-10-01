package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

public class KeystrokesHudModule extends HudModule {

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean lastLeftPressed = false;
    private boolean lastRightPressed = false;

    public KeystrokesHudModule() {
        super("keystrokes", "Keystrokes", "Displays reactive WASD, Space, and mouse buttons", 6, 245, 68, 86, false);
    }

    private void updateCps() {
        long now = System.currentTimeMillis();
        boolean leftPressed = client.options.attackKey.isPressed();
        boolean rightPressed = client.options.useKey.isPressed();

        if (leftPressed && !lastLeftPressed) {
            leftClicks.add(now);
        }
        if (rightPressed && !lastRightPressed) {
            rightClicks.add(now);
        }

        lastLeftPressed = leftPressed;
        lastRightPressed = rightPressed;

        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        updateCps();

        int keySize = 20;
        int gap = 2;
        this.width = (keySize * 3) + (gap * 2) + 4;
        this.height = (keySize * 4) + (gap * 3) + 4;

        renderBackground(context, width, height);

        int baseX = x + 2;
        int baseY = y + 2;

        // W key (centered in top row)
        boolean wPressed = client.options.forwardKey.isPressed();
        drawKey(context, "W", baseX + keySize + gap, baseY, keySize, keySize, wPressed);

        // A, S, D keys
        boolean aPressed = client.options.leftKey.isPressed();
        boolean sPressed = client.options.backKey.isPressed();
        boolean dPressed = client.options.rightKey.isPressed();
        int row2Y = baseY + keySize + gap;
        drawKey(context, "A", baseX, row2Y, keySize, keySize, aPressed);
        drawKey(context, "S", baseX + keySize + gap, row2Y, keySize, keySize, sPressed);
        drawKey(context, "D", baseX + (keySize + gap) * 2, row2Y, keySize, keySize, dPressed);

        // LMB & RMB
        int row3Y = row2Y + keySize + gap;
        int mouseW = (width - 4 - gap) / 2;
        boolean lmbPressed = client.options.attackKey.isPressed();
        boolean rmbPressed = client.options.useKey.isPressed();
        drawKeyWithSubtext(context, "LMB", leftClicks.size() + " CPS", baseX, row3Y, mouseW, keySize, lmbPressed);
        drawKeyWithSubtext(context, "RMB", rightClicks.size() + " CPS", baseX + mouseW + gap, row3Y, mouseW, keySize, rmbPressed);

        // Spacebar
        int row4Y = row3Y + keySize + gap;
        boolean spacePressed = client.options.jumpKey.isPressed();
        drawKey(context, "---", baseX, row4Y, width - 4, 12, spacePressed);
    }

    private void drawKey(DrawContext context, String label, int kx, int ky, int kw, int kh, boolean pressed) {
        int bgColor = pressed ? ColorUtil.PRIMARY : ColorUtil.BACKGROUND_CARD;
        int textColor = pressed ? 0xFF16171B : ColorUtil.SECONDARY;
        int borderColor = pressed ? ColorUtil.PRIMARY : ColorUtil.CARD_BORDER;

        RenderUtil.drawCard(context, kx, ky, kw, kh, bgColor, borderColor);
        RenderUtil.drawCenteredText(context, client.textRenderer, label, kx + (kw / 2), ky + (kh / 2) - 4, textColor);
    }

    private void drawKeyWithSubtext(DrawContext context, String label, String sub, int kx, int ky, int kw, int kh, boolean pressed) {
        int bgColor = pressed ? ColorUtil.PRIMARY : ColorUtil.BACKGROUND_CARD;
        int textColor = pressed ? 0xFF16171B : ColorUtil.SECONDARY;
        int borderColor = pressed ? ColorUtil.PRIMARY : ColorUtil.CARD_BORDER;

        RenderUtil.drawCard(context, kx, ky, kw, kh, bgColor, borderColor);
        RenderUtil.drawCenteredText(context, client.textRenderer, label, kx + (kw / 2), ky + 2, textColor);
        RenderUtil.drawCenteredText(context, client.textRenderer, sub, kx + (kw / 2), ky + 10, pressed ? 0xFF333333 : ColorUtil.TEXT_MUTED);
    }
}
