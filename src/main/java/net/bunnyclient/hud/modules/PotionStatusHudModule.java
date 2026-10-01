package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public class PotionStatusHudModule extends HudModule {

    public PotionStatusHudModule() {
        super("potions", "Potion Status", "Displays active potion effects and countdown timers", 6, 175, 95, 60, true);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        Collection<StatusEffectInstance> effects = player.getStatusEffects();
        if (effects.isEmpty()) {
            return;
        }

        int rowHeight = 18;
        this.height = (effects.size() * rowHeight) + 8;
        int maxW = 85;

        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int w = client.textRenderer.getWidth(name);
            if (w + 40 > maxW) {
                maxW = w + 40;
            }
        }
        this.width = maxW;

        renderBackground(context, width, height);

        int currentY = y + 4;
        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier();
            if (amp > 0) {
                name += " " + (amp + 1);
            }

            int totalSeconds = effect.getDuration() / 20;
            String time = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);

            int nameColor = effect.getEffectType().value().isBeneficial() ? ColorUtil.PRIMARY : ColorUtil.ACCENT_RED;
            context.drawText(client.textRenderer, name, x + 6, currentY + 1, nameColor, true);
            context.drawText(client.textRenderer, time, x + 6, currentY + 10, ColorUtil.SECONDARY, true);

            currentY += rowHeight;
        }
    }
}
