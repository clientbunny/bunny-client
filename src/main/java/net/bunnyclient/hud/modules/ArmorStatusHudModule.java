package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorStatusHudModule extends HudModule {

    public ArmorStatusHudModule() {
        super("armor", "Armor Status", "Displays equipped armor durability and held item", 6, 75, 75, 96, true);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        List<ItemStack> items = new ArrayList<>();
        // Armor slots are now 36-39 (boots to helmet)
        for (int i = 3; i >= 0; i--) {
            ItemStack armor = player.getInventory().getStack(36 + i);
            if (!armor.isEmpty()) {
                items.add(armor);
            }
        }
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()) {
            items.add(mainHand);
        }

        if (items.isEmpty()) {
            this.height = 20;
            this.width = 60;
            return;
        }

        int itemRowHeight = 18;
        this.height = (items.size() * itemRowHeight) + 8;
        this.width = 82;

        renderBackground(context, width, height);

        int currentY = y + 4;
        for (ItemStack stack : items) {
            context.drawItem(stack, x + 4, currentY);
            // The last argument (string) is for a custom count text. Empty string uses default.
            context.drawItem(stack, x + 4, currentY);

            String statusText;
            int textColor = ColorUtil.PRIMARY;

            if (stack.isDamageable()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamage();
                int remaining = maxDamage - currentDamage;
                statusText = String.valueOf(remaining);

                float ratio = (float) remaining / (float) maxDamage;
                if (ratio < 0.2f) {
                    textColor = ColorUtil.ACCENT_RED;
                } else if (ratio < 0.5f) {
                    textColor = ColorUtil.SECONDARY;
                }
            } else {
                statusText = stack.getCount() > 1 ? ("x" + stack.getCount()) : "";
            }

            if (!statusText.isEmpty()) {
                context.drawText(client.textRenderer, statusText, x + 24, currentY + 5, textColor, true);
            }

            currentY += itemRowHeight;
        }
    }
}