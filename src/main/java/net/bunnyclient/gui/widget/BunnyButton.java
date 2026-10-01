package net.bunnyclient.gui.widget;

import net.bunnyclient.util.BunnySoundUtil;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

/** Rounded button with smooth hover animation. primaryStyle = filled accent button. */
public class BunnyButton extends ClickableWidget {
    private final Runnable onPress;
    private final boolean primaryStyle;
    private float hover = 0f;

    public BunnyButton(int x, int y, int width, int height, Text message, boolean primaryStyle, Runnable onPress) {
        super(x, y, width, height, message);
        this.primaryStyle = primaryStyle;
        this.onPress = onPress;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        BunnySoundUtil.playClick();
        if (onPress != null) onPress.run();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        hover = RenderUtil.approach(hover, isHovered() ? 1f : 0f, 0.28f);

        int fill, stroke, text;
        if (primaryStyle) {
            fill = ColorUtil.lerp(ColorUtil.PRIMARY, 0xFFEFE4E0, hover);
            stroke = ColorUtil.lerp(0xFFB7A8A3, 0xFFFFFFFF, hover);
            text = 0xFF16171B;
        } else {
            fill = ColorUtil.lerp(ColorUtil.CARD, ColorUtil.CARD_HOVER, hover);
            stroke = ColorUtil.lerp(ColorUtil.STROKE, ColorUtil.PRIMARY, hover);
            text = ColorUtil.lerp(ColorUtil.SECONDARY, ColorUtil.PRIMARY, hover);
        }

        // small lift on hover
        int lift = Math.round(hover);
        RenderUtil.roundedCard(context, getX(), getY() - lift, getWidth(), getHeight(), 5, fill, stroke);
        RenderUtil.drawCenteredText(context, MinecraftClient.getInstance().textRenderer, getMessage(),
                getX() + getWidth() / 2, getY() - lift + (getHeight() - 8) / 2, text);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
