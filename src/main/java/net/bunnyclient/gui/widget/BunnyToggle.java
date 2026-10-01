package net.bunnyclient.gui.widget;

import net.bunnyclient.util.BunnySoundUtil;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** Pill-shaped switch with an animated knob. */
public class BunnyToggle extends ClickableWidget {
    private boolean toggled;
    private float anim;
    private final Consumer<Boolean> onToggle;

    public BunnyToggle(int x, int y, boolean initialValue, Consumer<Boolean> onToggle) {
        super(x, y, 30, 14, Text.empty());
        this.toggled = initialValue;
        this.anim = initialValue ? 1f : 0f;
        this.onToggle = onToggle;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        toggled = !toggled;
        BunnySoundUtil.playToggle(toggled);
        if (onToggle != null) onToggle.accept(toggled);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        anim = RenderUtil.approach(anim, toggled ? 1f : 0f, 0.3f);

        int track = ColorUtil.lerp(0xFF2A2D36, 0xFF4E9B6B, anim);
        int stroke = ColorUtil.lerp(isHovered() ? ColorUtil.STROKE_HOVER : ColorUtil.STROKE, 0xFF7FD79B, anim);
        RenderUtil.roundedCard(context, getX(), getY(), getWidth(), getHeight(), 7, track, stroke);

        int knob = 10;
        int knobX = getX() + 2 + Math.round((getWidth() - knob - 4) * anim);
        RenderUtil.fillRounded(context, knobX, getY() + 2, knob, knob, 5,
                ColorUtil.lerp(ColorUtil.SECONDARY, 0xFFFFFFFF, anim));
    }

    public boolean isToggled() { return toggled; }
    public void setToggled(boolean toggled) { this.toggled = toggled; }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
