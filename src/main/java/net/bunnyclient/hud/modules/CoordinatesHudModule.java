package net.bunnyclient.hud.modules;

import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CoordinatesHudModule extends HudModule {

    public CoordinatesHudModule() {
        super("coords", "Coordinates", "Displays player position, direction, and biome", 6, 26, 120, 42, true);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!enabled) return;

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        int posX = (int) Math.floor(player.getX());
        int posY = (int) Math.floor(player.getY());
        int posZ = (int) Math.floor(player.getZ());

        String xyz = String.format("XYZ: %d, %d, %d", posX, posY, posZ);

        Direction dir = player.getHorizontalFacing();
        String axis = switch (dir) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            default -> "";
        };
        String facing = "Facing: " + dir.asString().toUpperCase() + " (" + axis + ")";

        // Changed from getWorld() to getEntityWorld()
        String netherCoords;
        if (player.getEntityWorld().getRegistryKey() == World.NETHER) {
            netherCoords = String.format("Overworld: %d, %d, %d", posX * 8, posY, posZ * 8);
        } else {
            netherCoords = String.format("Nether: %d, %d, %d", posX / 8, posY, posZ / 8);
        }

        int maxTextWidth = Math.max(client.textRenderer.getWidth(xyz),
                           Math.max(client.textRenderer.getWidth(facing),
                                    client.textRenderer.getWidth(netherCoords)));

        this.width = maxTextWidth + 14;
        this.height = 38;

        renderBackground(context, width, height);

        int textY = y + 4;
        context.drawText(client.textRenderer, xyz, x + 6, textY, ColorUtil.PRIMARY, true);
        context.drawText(client.textRenderer, facing, x + 6, textY + 11, ColorUtil.SECONDARY, true);
        context.drawText(client.textRenderer, netherCoords, x + 6, textY + 22, ColorUtil.TEXT_MUTED, true);
    }
}