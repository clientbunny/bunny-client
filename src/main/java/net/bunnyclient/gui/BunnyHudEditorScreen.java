package net.bunnyclient.gui;

import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.gui.widget.BunnyButton;
import net.bunnyclient.hud.HudManager;
import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.BunnySoundUtil;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BunnyHudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule draggingModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public BunnyHudEditorScreen(Screen parent) {
        super(Text.literal("Bunny HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();

        int btnW = 100;
        int btnH = 20;

        this.addDrawableChild(new BunnyButton(
            (this.width / 2) - btnW - 6,
            this.height - 30,
            btnW,
            btnH,
            Text.literal("Reset Defaults"),
            false,
            () -> {
                ConfigManager.getConfig().initDefaults();
                HudManager.getInstance().resetPositions();
                BunnySoundUtil.playClick();
            }
        ));

        this.addDrawableChild(new BunnyButton(
            (this.width / 2) + 6,
            this.height - 30,
            btnW,
            btnH,
            Text.literal("Save & Exit"),
            true,
            () -> {
                for (HudModule m : HudManager.getInstance().getModules()) {
                    m.saveToConfig();
                }
                ConfigManager.save();
                if (this.client != null) {
                    this.client.setScreen(parent);
                }
            }
        ));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.buttonInfo().button() == 0) {
            for (HudModule module : HudManager.getInstance().getModules()) {
                if (module.isEnabled() && module.isHovered((int) click.x(), (int) click.y())) {
                    draggingModule = module;
                    dragOffsetX = (int) click.x() - module.getX();
                    dragOffsetY = (int) click.y() - module.getY();
                    BunnySoundUtil.playClick();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.buttonInfo().button() == 0 && draggingModule != null) {
            draggingModule.saveToConfig();
            draggingModule = null;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingModule != null) {
            int newX = (int) click.x() - dragOffsetX;
            int newY = (int) click.y() - dragOffsetY;

            int snapDist = 8;
            if (Math.abs(newX) < snapDist) newX = 4;
            if (Math.abs((newX + draggingModule.getWidth()) - this.width) < snapDist) {
                newX = this.width - draggingModule.getWidth() - 4;
            }
            if (Math.abs(newY) < snapDist) newY = 4;
            if (Math.abs((newY + draggingModule.getHeight()) - this.height) < snapDist) {
                newY = this.height - draggingModule.getHeight() - 4;
            }

            draggingModule.setX(newX);
            draggingModule.setY(newY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x990A0B0E);

        context.fill((this.width / 2) - 1, 0, this.width / 2, this.height, 0x22FFFFFF);
        context.fill(0, (this.height / 2) - 1, this.width, this.height / 2, 0x22FFFFFF);

        String banner = "Click & Drag any HUD module to reposition. Snapping is enabled.";
        RenderUtil.drawCenteredText(context, this.textRenderer, banner, this.width / 2, 14, ColorUtil.PRIMARY);

        for (HudModule module : HudManager.getInstance().getModules()) {
            if (module.isEnabled()) {
                module.renderEditor(context);

                boolean hovered = module.isHovered(mouseX, mouseY) || module == draggingModule;
                int outlineColor = hovered ? ColorUtil.PRIMARY : 0x66B9C0C4;
                RenderUtil.drawBorder(context, module.getX() - 1, module.getY() - 1, module.getWidth() + 2, module.getHeight() + 2, outlineColor);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        for (HudModule m : HudManager.getInstance().getModules()) {
            m.saveToConfig();
        }
        ConfigManager.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}