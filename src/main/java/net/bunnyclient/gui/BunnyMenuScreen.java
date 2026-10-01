package net.bunnyclient.gui;

import net.bunnyclient.BunnyClient;
import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.features.*;
import net.bunnyclient.gui.widget.BunnyButton;
import net.bunnyclient.gui.widget.BunnySlider;
import net.bunnyclient.gui.widget.BunnyToggle;
import net.bunnyclient.hud.HudManager;
import net.bunnyclient.hud.HudModule;
import net.bunnyclient.util.BunnySoundUtil;
import net.bunnyclient.util.ColorUtil;
import net.bunnyclient.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Lunar-style mod menu: sidebar, search box, category chips and a scrollable grid of mod cards.
 * All non-widget drawing happens in renderBackground() so it is never covered by vanilla blur/dim.
 */
public class BunnyMenuScreen extends Screen {

    private enum Tab {
        MODS("Mods"),
        SETTINGS("Settings");
        final String title;
        Tab(String title) { this.title = title; }
    }

    private enum Category {
        ALL("All", 0xFFD6C8C3),
        HUD("HUD", 0xFF7FB8E8),
        GAMEPLAY("Gameplay", 0xFF8FD6A0),
        PERFORMANCE("Performance", 0xFFE8C97F),
        EXTRAS("Extras", 0xFFE89FB8);
        final String title;
        final int color;
        Category(String title, int color) { this.title = title; this.color = color; }
    }

    /** One card in the grid. */
    private static final class ModEntry {
        final String id, name, desc;
        final Category category;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        float anim = -1f;

        ModEntry(String id, String name, String desc, Category category, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.id = id; this.name = name; this.desc = desc; this.category = category;
            this.getter = getter; this.setter = setter;
        }
    }

    // remembered between re-inits
    private static Tab currentTab = Tab.MODS;
    private static Category currentCategory = Category.ALL;
    private static String searchText = "";

    private final List<ModEntry> allEntries = new ArrayList<>();
    private final List<ModEntry> visible = new ArrayList<>();
    private final Map<String, Float> hoverAnim = new HashMap<>();

    private TextFieldWidget searchField;

    private int panelX, panelY, panelW, panelH;
    private final int sidebarW = 122;
    private int gridX, gridY, gridW, gridH;
    private static final int CARD_H = 44;
    private static final int GAP = 8;
    private int columns = 2;

    private float scroll = 0f;
    private float scrollTarget = 0f;

    public BunnyMenuScreen() {
        super(Text.literal("Bunny Client"));
    }

    // ------------------------------------------------------------ setup

    private void buildEntries() {
        allEntries.clear();
        var cfg = ConfigManager.getConfig();

        for (HudModule m : HudManager.getInstance().getModules()) {
            allEntries.add(new ModEntry("hud_" + m.getId(), m.getName(), m.getDescription(), Category.HUD,
                    m::isEnabled, v -> m.setEnabled(v)));
        }

        allEntries.add(new ModEntry("zoom", "Zoom", "Hold a key to zoom in smoothly", Category.GAMEPLAY,
                () -> cfg.zoomEnabled, v -> { cfg.zoomEnabled = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("fullbright", "Fullbright", "See clearly in dark caves", Category.GAMEPLAY,
                () -> cfg.fullbright, FullbrightFeature::setEnabled));
        allEntries.add(new ModEntry("togglesprint", "Toggle Sprint", "Sprint without holding the key", Category.GAMEPLAY,
                () -> cfg.toggleSprint, v -> { cfg.toggleSprint = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("togglesneak", "Toggle Sneak", "Press once to keep sneaking", Category.GAMEPLAY,
                () -> cfg.toggleSneak, v -> { cfg.toggleSneak = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("hitboxes", "Entity Hitboxes", "Draw bounding boxes around entities", Category.GAMEPLAY,
                () -> cfg.hitboxes, HitboxManager::setEnabled));
        allEntries.add(new ModEntry("crosshair", "Custom Crosshair", "Dot or cross style reticle", Category.GAMEPLAY,
                () -> cfg.customCrosshair, v -> { cfg.customCrosshair = v; ConfigManager.save(); }));

        allEntries.add(new ModEntry("culling", "Entity Culling", "Skip far, off-screen entities", Category.PERFORMANCE,
                () -> cfg.entityCulling, v -> { cfg.entityCulling = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("particles", "Particle Limiter", "Fewer particles in big fights", Category.PERFORMANCE,
                () -> cfg.particleLimiter, v -> { cfg.particleLimiter = v; ConfigManager.save(); }));

        allEntries.add(new ModEntry("badge", "Bunny Badge", "Bunny icon next to Bunny Client users", Category.EXTRAS,
                () -> cfg.bunnyBadge, v -> { cfg.bunnyBadge = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("title", "Bunny Title Screen", "Replace the vanilla main menu", Category.EXTRAS,
                () -> cfg.lunarTitleScreen, v -> { cfg.lunarTitleScreen = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("rpc", "Discord Rich Presence", "Show Bunny Client on your profile", Category.EXTRAS,
                () -> cfg.discordRpc, v -> { cfg.discordRpc = v; ConfigManager.save(); }));
        allEntries.add(new ModEntry("rpcserver", "Show Server in Discord", "Include the server address in RPC", Category.EXTRAS,
                () -> cfg.discordShowServer, v -> { cfg.discordShowServer = v; ConfigManager.save(); }));
    }

    private void applyFilter() {
        visible.clear();
        String q = searchText.toLowerCase(Locale.ROOT).trim();
        for (ModEntry e : allEntries) {
            if (currentCategory != Category.ALL && e.category != currentCategory) continue;
            if (!q.isEmpty() && !e.name.toLowerCase(Locale.ROOT).contains(q)
                    && !e.desc.toLowerCase(Locale.ROOT).contains(q)) continue;
            visible.add(e);
        }
        scroll = scrollTarget = 0f;
    }

    @Override
    protected void init() {
        this.clearChildren();
        buildEntries();

        panelW = Math.min(660, this.width - 24);
        panelH = Math.min(390, this.height - 24);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // ---- sidebar tabs
        int tabY = panelY + 58;
        for (Tab tab : Tab.values()) {
            final Tab t = tab;
            this.addDrawableChild(new BunnyButton(panelX + 10, tabY, sidebarW - 20, 22,
                    Text.literal(tab.title), currentTab == tab, () -> {
                if (currentTab != t) {
                    currentTab = t;
                    BunnySoundUtil.playTabSwitch();
                    this.init();
                }
            }));
            tabY += 28;
        }

        // ---- header: edit HUD
        int contentX = panelX + sidebarW + 10;
        int contentW = panelW - sidebarW - 20;
        this.addDrawableChild(new BunnyButton(contentX + contentW - 96, panelY + 12, 96, 20,
                Text.literal("Edit HUD"), true, () -> {
            if (this.client != null) this.client.setScreen(new BunnyHudEditorScreen(this));
        }));

        if (currentTab == Tab.MODS) initModsTab(contentX, contentW);
        else initSettingsTab(contentX, contentW);
    }

    private void initModsTab(int contentX, int contentW) {
        // search
        searchField = new TextFieldWidget(this.textRenderer, contentX + 8, panelY + 18, contentW - 136, 12, Text.literal("Search"));
        searchField.setDrawsBackground(false);
        searchField.setMaxLength(32);
        searchField.setPlaceholder(Text.literal("Search mods..."));
        searchField.setText(searchText);
        searchField.setChangedListener(s -> { searchText = s; applyFilter(); });
        this.addDrawableChild(searchField);

        // category chips
        int chipX = contentX;
        int chipY = panelY + 42;
        for (Category cat : Category.values()) {
            final Category c = cat;
            int w = this.textRenderer.getWidth(cat.title) + 18;
            this.addDrawableChild(new BunnyButton(chipX, chipY, w, 16, Text.literal(cat.title), currentCategory == cat, () -> {
                currentCategory = c;
                this.init();
            }));
            chipX += w + 5;
        }

        gridX = contentX;
        gridY = panelY + 66;
        gridW = contentW - 6;
        gridH = panelY + panelH - 12 - gridY;
        columns = contentW >= 400 ? 2 : 1;
        applyFilter();
    }

    private void initSettingsTab(int contentX, int contentW) {
        var cfg = ConfigManager.getConfig();
        int x = contentX;
        int y = panelY + 50;

        // Zoom FOV slider (row 1)
        this.addDrawableChild(new BunnySlider(x + contentW - 130, y + 20, 116, 16, 10f, 60f, cfg.zoomFov, "\u00B0", v -> {
            cfg.zoomFov = v;
            ConfigManager.save();
        }));
        y += 48;

        // Smooth zoom toggle (row 2)
        this.addDrawableChild(new BunnyToggle(x + contentW - 44, y + 13, cfg.smoothZoom, v -> {
            cfg.smoothZoom = v;
            ConfigManager.save();
        }));
    }

    // ------------------------------------------------------------ drawing

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // dim only (no blur) so our panel stays crisp
        ctx.fillGradient(0, 0, this.width, this.height, 0xB0080A0E, 0xD0101218);

        RenderUtil.roundedCard(ctx, panelX, panelY, panelW, panelH, 8, ColorUtil.BG_PANEL, ColorUtil.STROKE);

        // sidebar
        RenderUtil.fillRounded(ctx, panelX + 1, panelY + 1, sidebarW, panelH - 2, 7, ColorUtil.BG_SIDEBAR);
        ctx.fill(panelX + sidebarW - 6, panelY + 1, panelX + sidebarW + 1, panelY + panelH - 1, ColorUtil.BG_SIDEBAR);
        ctx.fill(panelX + sidebarW + 1, panelY + 8, panelX + sidebarW + 2, panelY + panelH - 8, ColorUtil.STROKE);

        // brand
        RenderUtil.logo(ctx, panelX + 12, panelY + 12, 28);
        ctx.drawText(this.textRenderer, "BUNNY", panelX + 46, panelY + 15, ColorUtil.PRIMARY, true);
        ctx.drawText(this.textRenderer, "CLIENT", panelX + 46, panelY + 26, ColorUtil.SECONDARY, false);
        ctx.drawText(this.textRenderer, "v" + BunnyClient.VERSION + " | 1.21.11", panelX + 12, panelY + panelH - 16, ColorUtil.TEXT_MUTED, false);

        if (currentTab == Tab.MODS) {
            renderModsBackground(ctx, mouseX, mouseY);
        } else {
            renderSettingsBackground(ctx);
        }
    }

    private void renderModsBackground(DrawContext ctx, int mouseX, int mouseY) {
        int contentX = panelX + sidebarW + 10;
        int contentW = panelW - sidebarW - 20;

        // search box backing
        RenderUtil.roundedCard(ctx, contentX, panelY + 12, contentW - 108, 20, 5, ColorUtil.CARD, ColorUtil.STROKE);

        // smooth scroll
        scroll += (scrollTarget - scroll) * 0.3f;
        int cardW = (gridW - (columns - 1) * GAP) / columns;
        int rows = (visible.size() + columns - 1) / columns;
        int contentH = Math.max(0, rows * (CARD_H + GAP) - GAP);
        int maxScroll = Math.max(0, contentH - gridH);
        scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));

        ctx.enableScissor(gridX, gridY, gridX + gridW, gridY + gridH);
        for (int i = 0; i < visible.size(); i++) {
            ModEntry e = visible.get(i);
            int col = i % columns;
            int row = i / columns;
            int cx = gridX + col * (cardW + GAP);
            int cy = gridY + row * (CARD_H + GAP) - Math.round(scroll);
            if (cy + CARD_H < gridY || cy > gridY + gridH) continue;
            drawCard(ctx, e, cx, cy, cardW, mouseX, mouseY);
        }
        ctx.disableScissor();

        if (visible.isEmpty()) {
            RenderUtil.drawCenteredText(ctx, this.textRenderer, "No mods match your search",
                    gridX + gridW / 2, gridY + 40, ColorUtil.TEXT_MUTED);
        }

        // scrollbar
        if (maxScroll > 0) {
            int trackX = gridX + gridW + 2;
            int barH = Math.max(18, (int) ((float) gridH * gridH / contentH));
            int barY = gridY + (int) ((gridH - barH) * (scroll / maxScroll));
            RenderUtil.fillRounded(ctx, trackX, barY, 3, barH, 1, ColorUtil.STROKE_HOVER);
        }
    }

    private void drawCard(DrawContext ctx, ModEntry e, int x, int y, int w, int mouseX, int mouseY) {
        boolean inGrid = mouseY >= gridY && mouseY <= gridY + gridH;
        boolean hovered = inGrid && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + CARD_H;
        float h = RenderUtil.approach(hoverAnim.getOrDefault(e.id, 0f), hovered ? 1f : 0f, 0.3f);
        hoverAnim.put(e.id, h);

        boolean on = e.getter.getAsBoolean();
        if (e.anim < 0f) e.anim = on ? 1f : 0f;
        e.anim = RenderUtil.approach(e.anim, on ? 1f : 0f, 0.3f);

        int fill = ColorUtil.lerp(ColorUtil.CARD, ColorUtil.CARD_HOVER, h);
        int stroke = ColorUtil.lerp(ColorUtil.STROKE, e.category.color, h * 0.8f);
        RenderUtil.roundedCard(ctx, x, y, w, CARD_H, 6, fill, stroke);

        // icon tile with initial
        int tileColor = ColorUtil.lerp(0xFF2A2D36, e.category.color, 0.25f + 0.55f * e.anim);
        RenderUtil.fillRounded(ctx, x + 8, y + 8, 28, 28, 6, tileColor);
        String initial = e.name.substring(0, 1).toUpperCase(Locale.ROOT);
        RenderUtil.drawCenteredText(ctx, this.textRenderer, initial, x + 22, y + 18, 0xFF15161B);

        // text
        int textX = x + 44;
        int textMax = w - 44 - 50;
        ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth(e.name, textMax), textX, y + 10, ColorUtil.PRIMARY, true);
        ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth(e.desc, textMax), textX, y + 23, ColorUtil.TEXT_MUTED, false);

        // switch
        int sx = x + w - 38;
        int sy = y + (CARD_H - 14) / 2;
        int track = ColorUtil.lerp(0xFF2A2D36, 0xFF4E9B6B, e.anim);
        int tStroke = ColorUtil.lerp(ColorUtil.STROKE, 0xFF7FD79B, e.anim);
        RenderUtil.roundedCard(ctx, sx, sy, 30, 14, 7, track, tStroke);
        int kx = sx + 2 + Math.round(16 * e.anim);
        RenderUtil.fillRounded(ctx, kx, sy + 2, 10, 10, 5, ColorUtil.lerp(ColorUtil.SECONDARY, 0xFFFFFFFF, e.anim));
    }

    private void renderSettingsBackground(DrawContext ctx) {
        int x = panelX + sidebarW + 10;
        int w = panelW - sidebarW - 20;
        int y = panelY + 50;

        // Zoom FOV row
        RenderUtil.roundedCard(ctx, x, y, w, 40, 6, ColorUtil.CARD, ColorUtil.STROKE);
        ctx.drawText(this.textRenderer, "Zoom FOV", x + 10, y + 9, ColorUtil.PRIMARY, true);
        ctx.drawText(this.textRenderer, "Field of view while zoom is held", x + 10, y + 22, ColorUtil.TEXT_MUTED, false);
        y += 48;

        // Smooth zoom row
        RenderUtil.roundedCard(ctx, x, y, w, 40, 6, ColorUtil.CARD, ColorUtil.STROKE);
        ctx.drawText(this.textRenderer, "Smooth Zoom", x + 10, y + 9, ColorUtil.PRIMARY, true);
        ctx.drawText(this.textRenderer, "Animate zooming in and out", x + 10, y + 22, ColorUtil.TEXT_MUTED, false);
        y += 48;

        // Badge network status
        RenderUtil.roundedCard(ctx, x, y, w, 40, 6, ColorUtil.CARD, ColorUtil.STROKE);
        String url = ConfigManager.getConfig().backendUrl;
        boolean set = url != null && !url.isBlank();
        ctx.drawText(this.textRenderer, "Badge Network", x + 10, y + 9, ColorUtil.PRIMARY, true);
        String status = set ? this.textRenderer.trimToWidth(url, w - 24) : "Not configured - badge shows on you only";
        ctx.drawText(this.textRenderer, status, x + 10, y + 22, set ? ColorUtil.ACCENT_GREEN : ColorUtil.TEXT_MUTED, false);
        ctx.drawText(this.textRenderer, "Set backendUrl in config/bunnyclient.json", x + 10, y + 46, ColorUtil.TEXT_MUTED, false);
    }

    // ------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;

        if (currentTab == Tab.MODS && click.button() == 0) {
            double mx = click.x(), my = click.y();
            if (mx >= gridX && mx < gridX + gridW && my >= gridY && my < gridY + gridH) {
                int cardW = (gridW - (columns - 1) * GAP) / columns;
                for (int i = 0; i < visible.size(); i++) {
                    int col = i % columns;
                    int row = i / columns;
                    int cx = gridX + col * (cardW + GAP);
                    int cy = gridY + row * (CARD_H + GAP) - Math.round(scroll);
                    if (mx >= cx && mx < cx + cardW && my >= cy && my < cy + CARD_H) {
                        ModEntry e = visible.get(i);
                        boolean next = !e.getter.getAsBoolean();
                        e.setter.accept(next);
                        BunnySoundUtil.playToggle(next);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == Tab.MODS) {
            scrollTarget -= (float) (verticalAmount * 26);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
