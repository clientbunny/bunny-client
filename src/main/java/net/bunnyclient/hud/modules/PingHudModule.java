package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

public class PingHudModule extends HudModule {

    public PingHudModule() {
        super("ping", "Ping Display", "Displays your connection latency in milliseconds", 140, 6, 52, 16, true);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        int ping = 0;
        if (client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        String pingText = ping + " ms";
        int textW = client.textRenderer.getWidth(pingText);
        this.width = Math.max(48, textW + 14);
        this.height = 16;

        renderBackground(context, width, height);

        int color = ping < 80 ? ColorUtil.PRIMARY : (ping < 160 ? ColorUtil.SECONDARY : ColorUtil.ACCENT_RED);
        context.drawText(client.textRenderer, pingText, x + 6, y + 4, color, true);
    }
}
