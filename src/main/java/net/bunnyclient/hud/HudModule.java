package net.bunnyclient.hud;

import net.bunnyclient.config.BunnyConfig;
import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Base class for all draggable, configurable Bunny Client HUD modules.
 * Fully compatible with VulkanMod rendering via DrawContext.
 */
public abstract class HudModule {
    protected final MinecraftClient client = MinecraftClient.getInstance();
    private final String id;
    private final String name;
    private final String description;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean enabled;
    protected boolean showBackground = true;
    protected boolean showBorder = true;

    public HudModule(String id, String name, String description, int defaultX, int defaultY, int defaultW, int defaultH, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.x = defaultX;
        this.y = defaultY;
        this.width = defaultW;
        this.height = defaultH;
        this.enabled = defaultEnabled;
        syncFromConfig();
    }

    public void syncFromConfig() {
        BunnyConfig.ModulePos pos = ConfigManager.getConfig().getModulePos(id, x, y, enabled);
        this.x = pos.x;
        this.y = pos.y;
        this.enabled = pos.enabled;
        this.showBackground = pos.background;
        this.showBorder = pos.border;
    }

    public void saveToConfig() {
        BunnyConfig.ModulePos pos = ConfigManager.getConfig().getModulePos(id, x, y, enabled);
        pos.x = this.x;
        pos.y = this.y;
        pos.enabled = this.enabled;
        pos.background = this.showBackground;
        pos.border = this.showBorder;
        ConfigManager.save();
    }

    /**
     * Renders standard card background and border if enabled.
     */
    protected void renderBackground(DrawContext context, int w, int h) {
        if (showBackground) {
            int borderColor = showBorder ? ColorUtil.CARD_BORDER : 0;
            RenderUtil.drawCard(context, x, y, w, h, ColorUtil.BACKGROUND_CARD, borderColor);
        }
    }

    /**
     * Called every in-game frame by HudRenderCallback.
     */
    public abstract void render(DrawContext context, RenderTickCounter tickCounter);

    /**
     * Called in the HUD layout editor. Default falls back to render().
     */
    public void renderEditor(DrawContext context) {
        render(context, null);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        saveToConfig();
    }
    public boolean isShowBackground() { return showBackground; }
    public void setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
        saveToConfig();
    }
    public boolean isShowBorder() { return showBorder; }
    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
        saveToConfig();
    }
}
