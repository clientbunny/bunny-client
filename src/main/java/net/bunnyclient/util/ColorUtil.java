package net.bunnyclient.util;

/**
 * Color utilities and theme constants for Bunny Client.
 * Primary palette: #D6C8C3 (Linen Cream) and #B9C0C4 (Slate Silver).
 */
public final class ColorUtil {
    private ColorUtil() {}

    // User requested theme colors
    public static final int PRIMARY = 0xFFD6C8C3;       // Warm linen / cream #D6C8C3
    public static final int SECONDARY = 0xFFB9C0C4;     // Cool slate silver #B9C0C4
    
    // UI Theme Palette
    public static final int BACKGROUND_DARK = 0xE616171B; // 90% opacity dark #16171B
    public static final int BACKGROUND_CARD = 0xE621242C; // Dark slate card #21242C
    public static final int CARD_BORDER = 0x44B9C0C4;     // Subtle slate border
    public static final int CARD_BORDER_HOVER = 0xAAECDCD7;
    public static final int ACCENT_ACTIVE = 0xFFD6C8C3;
    public static final int ACCENT_GREEN = 0xFF69DB7C;    // Soft mint green for toggled ON
    public static final int ACCENT_RED = 0xFFFF6B6B;      // Soft coral for toggled OFF
    public static final int TEXT_TITLE = 0xFFF8F9FA;
    public static final int TEXT_MUTED = 0xFF9CA3AF;
    public static final int OVERLAY_SHADOW = 0x55000000;
    public static final int HUD_DEFAULT_BG = 0x88000000;

    // Opaque palette used by the redesigned menus
    public static final int BG_DEEP = 0xFF0E0F13;
    public static final int BG_PANEL = 0xFF15161B;
    public static final int BG_SIDEBAR = 0xFF111217;
    public static final int CARD = 0xFF1C1E25;
    public static final int CARD_HOVER = 0xFF252832;
    public static final int STROKE = 0xFF2B2E38;
    public static final int STROKE_HOVER = 0xFF4A4E5C;

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int lerp(int color1, int color2, float factor) {
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return rgba(r, g, b, a);
    }
}
