package net.bunnyclient.gui.widget;

import net.bunnyclient.util.BunnySoundUtil;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click; // Added
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class BunnySlider extends ClickableWidget {
    private final float min;
    private final float max;
    private float value;
    private final String suffix;
    private final Consumer<Float> onChange;
    private boolean dragging = false;

    public BunnySlider(int x, int y, int width, int height, float min, float max, float initialValue, String suffix, Consumer<Float> onChange) {
        super(x, y, width, height, Text.empty());
        this.min = min;
        this.max = max;
        this.value = initialValue;
        this.suffix = suffix;
        this.onChange = onChange;
    }

    private void updateValue(double mouseX) {
        float factor = (float) ((mouseX - getX()) / (double) getWidth());
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        float newValue = min + factor * (max - min);
        if (Math.abs(newValue - this.value) > 0.01f) {
            this.value = newValue;
            BunnySoundUtil.playSliderTick();
            if (onChange != null) {
                onChange.accept(this.value);
            }
        }
    }

 @Override
public void onClick(Click click, boolean doubled) {
    this.dragging = true;
    updateValue(click.x());
}

@Override
protected void onDrag(Click click, double offsetX, double offsetY) {
    if (this.dragging) {
        updateValue(click.x());
    }
}

    @Override
    public void onRelease(Click click) { // Changed signature
        this.dragging = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int trackHeight = 4;
        int trackY = getY() + (getHeight() / 2) - (trackHeight / 2);

        RenderUtil.fillRounded(context, getX(), trackY, getWidth(), trackHeight, 2, 0xFF2A2D36);

        float progress = (value - min) / (max - min);
        int filledWidth = (int) (progress * getWidth());
        if (filledWidth > 0) RenderUtil.fillRounded(context, getX(), trackY, filledWidth, trackHeight, 2, ColorUtil.PRIMARY);

        int knobWidth = 8;
        int knobHeight = 14;
        int knobX = getX() + filledWidth - (knobWidth / 2);
        int knobY = getY() + (getHeight() / 2) - (knobHeight / 2);

        RenderUtil.fillRounded(context, knobX, knobY, knobWidth, knobHeight, 3, isHovered() || dragging ? 0xFFFFFFFF : ColorUtil.PRIMARY);

        String valText = String.format("%.0f%s", value, suffix);
        RenderUtil.drawCenteredText(context, MinecraftClient.getInstance().textRenderer, valText, getX() + (getWidth() / 2), getY() - 10, ColorUtil.TEXT_MUTED);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}