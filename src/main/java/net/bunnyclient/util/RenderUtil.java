package net.bunnyclient.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

/**
 * Pure DrawContext-based rendering helper functions.
 * Designed to be 100% compatible with VulkanMod, Sodium, Iris, and vanilla renderer.
 * Zero raw OpenGL / GL11 dependencies.
 */
public final class RenderUtil {
    private RenderUtil() {}

    /**
     * Draws a card container with rounded aesthetics and a clean border.
     */
    public static void drawCard(DrawContext context, int x, int y, int width, int height, int bgColor, int borderColor) {
        // Main body
        context.fill(x + 1, y, x + width - 1, y + height, bgColor);
        context.fill(x, y + 1, x + width, y + height - 1, bgColor);

        // Subtle drop shadow / outer border
        if ((borderColor & 0xFF000000) != 0) {
            context.fill(x + 1, y, x + width - 1, y + 1, borderColor);
            context.fill(x + 1, y + height - 1, x + width - 1, y + height, borderColor);
            context.fill(x, y + 1, x + 1, y + height - 1, borderColor);
            context.fill(x + width - 1, y + 1, x + width, y + height - 1, borderColor);
        }
    }

    /**
     * Draws a rectangular outline border.
     */
    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /**
     * Draws centered text horizontally.
     */
    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        int w = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, centerX - (w / 2), y, color, false);
    }

    public static void drawCenteredTextWithShadow(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        int w = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, centerX - (w / 2), y, color, true);
    }

    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        int w = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, centerX - (w / 2), y, color, false);
    }

    /**
     * Draws a badge / tag pill (e.g. "[🐰 Bunny]").
     */
    public static void drawPill(DrawContext context, TextRenderer textRenderer, String label, int x, int y, int bgColor, int textColor) {
        int textWidth = textRenderer.getWidth(label);
        int padding = 4;
        int height = 12;
        int totalWidth = textWidth + (padding * 2);

        drawCard(context, x, y, totalWidth, height, bgColor, 0x00000000);
        context.drawText(textRenderer, label, x + padding, y + 2, textColor, false);
    }

    public static final Identifier LOGO = Identifier.of("bunnyclient", "icon.png");

    /** Filled rectangle with circular corners (scanline approximation, no GL needed). */
    public static void fillRounded(DrawContext c, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { c.fill(x, y, x + w, y + h, color); return; }
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
        c.fill(x, y + r, x + w, y + h - r, color);
    }

    /** Rounded card with 1px stroke. Use opaque colours for best results. */
    public static void roundedCard(DrawContext c, int x, int y, int w, int h, int r, int fill, int stroke) {
        fillRounded(c, x, y, w, h, r, stroke);
        fillRounded(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    /** Draws the bunny logo scaled to size x size. */
    public static void logo(DrawContext c, int x, int y, int size) {
        c.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, x, y, 0f, 0f, size, size, 64, 64, 64, 64);
    }

    public static float approach(float cur, float target, float speed) {
        float d = target - cur;
        if (Math.abs(d) < 0.002f) return target;
        return cur + d * speed;
    }
}
