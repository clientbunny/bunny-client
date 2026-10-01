package net.bunnyclient.gui;

import net.bunnyclient.BunnyClient;
import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.gui.widget.BunnyButton;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * Lunar-inspired main menu: dark animated backdrop, centered logo + wordmark,
 * wide Singleplayer / Multiplayer buttons and a row of secondary actions.
 * Everything that is not a widget is drawn in renderBackground().
 */
public class BunnyTitleScreen extends Screen {

    // soft "bokeh" blobs: x, y (0..1), radius, speed, phase, color
    private static final float[][] BLOBS = {
        {0.15f, 0.25f, 46f, 0.00020f, 0.0f},
        {0.82f, 0.18f, 62f, 0.00014f, 1.7f},
        {0.30f, 0.80f, 70f, 0.00012f, 3.1f},
        {0.72f, 0.72f, 52f, 0.00018f, 4.4f},
        {0.50f, 0.50f, 90f, 0.00010f, 2.2f},
        {0.92f, 0.62f, 38f, 0.00022f, 5.5f},
        {0.06f, 0.62f, 34f, 0.00021f, 0.9f},
    };

    private int cardX, cardY, cardW, cardH;

    public BunnyTitleScreen() {
        super(Text.literal("Bunny Client"));
    }

    @Override
    protected void init() {
        this.clearChildren();

        int centerX = this.width / 2;
        int btnW = 200;
        int btnH = 24;
        int gap = 6;

        cardW = 250;
        cardH = 190;
        cardX = centerX - cardW / 2;
        cardY = Math.max(10, this.height / 2 - cardH / 2 + 6);

        int x = centerX - btnW / 2;
        int y = cardY + 92;

        this.addDrawableChild(new BunnyButton(x, y, btnW, btnH, Text.literal("Singleplayer"), true, () -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }));
        y += btnH + gap;

        this.addDrawableChild(new BunnyButton(x, y, btnW, btnH, Text.literal("Multiplayer"), false, () -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }));
        y += btnH + gap;

        // secondary row: Mods | Options | Quit
        int third = (btnW - gap * 2) / 3;
        this.addDrawableChild(new BunnyButton(x, y, third, 20, Text.literal("Mods"), false, () -> {
            if (this.client != null) this.client.setScreen(new BunnyMenuScreen());
        }));
        this.addDrawableChild(new BunnyButton(x + third + gap, y, third, 20, Text.literal("Options"), false, () -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }));
        this.addDrawableChild(new BunnyButton(x + (third + gap) * 2, y, btnW - (third + gap) * 2, 20, Text.literal("Quit"), false, () -> {
            if (this.client != null) this.client.scheduleStop();
        }));

        // vanilla fallback, top-right
        this.addDrawableChild(new BunnyButton(this.width - 96 - 10, 10, 96, 18, Text.literal("Vanilla Menu"), false, () -> {
            ConfigManager.getConfig().lunarTitleScreen = false;
            ConfigManager.save();
            if (this.client != null) this.client.setScreen(new TitleScreen());
        }));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // base gradient
        ctx.fillGradient(0, 0, this.width, this.height, 0xFF0C0D11, 0xFF1A1C25);

        // drifting soft blobs (stacked translucent discs fake a glow)
        long t = Util.getMeasuringTimeMs();
        for (float[] b : BLOBS) {
            float bx = b[0] * this.width + (float) Math.sin(t * b[3] + b[4]) * 40f;
            float by = b[1] * this.height + (float) Math.cos(t * b[3] * 1.3f + b[4]) * 28f;
            int r = (int) b[2];
            for (int layer = 3; layer >= 1; layer--) {
                int rr = r * layer / 3;
                int alpha = 10 + (4 - layer) * 4;
                RenderUtil.fillRounded(ctx, (int) bx - rr, (int) by - rr, rr * 2, rr * 2, rr,
                        ColorUtil.withAlpha(ColorUtil.PRIMARY, alpha));
            }
        }

        // center card
        RenderUtil.roundedCard(ctx, cardX, cardY, cardW, cardH, 10, 0xF0121318, ColorUtil.STROKE);

        // logo + wordmark
        int centerX = this.width / 2;
        RenderUtil.logo(ctx, centerX - 24, cardY + 12, 48);

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(centerX, cardY + 64f);
        m.scale(1.6f, 1.6f);
        String word = "BUNNY CLIENT";
        ctx.drawText(this.textRenderer, word, -this.textRenderer.getWidth(word) / 2, 0, ColorUtil.PRIMARY, true);
        m.popMatrix();

        RenderUtil.drawCenteredText(ctx, this.textRenderer, "Minecraft 1.21.11", centerX, cardY + 80, ColorUtil.TEXT_MUTED);

        // footer
        String user = this.client != null ? this.client.getSession().getUsername() : "";
        ctx.drawText(this.textRenderer, "Logged in as " + user, 10, this.height - 16, ColorUtil.TEXT_MUTED, false);
        String right = "Bunny Client v" + BunnyClient.VERSION;
        ctx.drawText(this.textRenderer, right, this.width - this.textRenderer.getWidth(right) - 10, this.height - 16, ColorUtil.TEXT_MUTED, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
