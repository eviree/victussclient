package com.victusclient.gui;

import com.victusclient.compat.Compat;
import com.victusclient.compat.VScreen;
import com.victusclient.module.ModuleManager;
import com.victusclient.module.modules.BrandingModule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** The Victus main menu (replaces the vanilla title screen). */
public class VictusTitleScreen extends VScreen {
    private static final int BTN_W = 184, BTN_H = 25, GAP = 6;
    private static final String MOD_VERSION = modVersion("victusclient");
    private static final String MC_VERSION = modVersion("minecraft");

    private final List<Hit> hits = new ArrayList<>();
    private final long openMs = System.currentTimeMillis();

    public VictusTitleScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    private static String modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id)
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
    }

    private Hit hit(int x, int y, int w, int h, Runnable left) {
        Hit b = new Hit(x, y, w, h);
        b.left = left;
        hits.add(b);
        return b;
    }

    // ---- render ----------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        Theme.apply();
        UI.alpha = 1f;
        hits.clear();

        drawBackdrop(ctx, mx, my, delta);
        ctx.fill(0, 0, width, height, 0x55000000);
        Fx.vignette(ctx, width, height);
        Fx.particles(ctx, width, height);

        // soft fade-in when the menu first appears
        float in = Math.min(1f, (System.currentTimeMillis() - openMs) / 450f);
        UI.alpha = in;

        BrandingModule br = ModuleManager.of(BrandingModule.class);
        String motion = br != null ? br.motion.get() : "Pulse";
        float speed = br != null ? br.motionSpeed() : 1f;

        int cx = width / 2;
        boolean compact = height < 260;
        int iconS = compact ? 0 : 40;
        float titleScale = compact ? 1.6f : 2.4f;
        int titleH = Math.round(9 * titleScale);
        int total = (iconS > 0 ? iconS + 10 : 0) + titleH + 4 + 10 + 18 + 4 * (BTN_H + GAP) + 8 + 20;
        int y = Math.max(10, (height - total) / 2 - 4);

        // A compact neon-glass frame gives the title screen the same visual language as ClickGUI.
        int panelW = 280, panelX = cx - panelW / 2;
        UI.card(ctx, panelX, y - 12, panelW, total + 20, 0xD10B0A16, UI.ACCENT_DIM);
        UI.fill(ctx, panelX + 2, y - 10, panelX + panelW - 2, y - 8, UI.ACCENT);
        UI.fill(ctx, panelX + 2, y + total + 6, panelX + 42, y + total + 8, UI.ACCENT);

        if (iconS > 0) {
            Logo.icon(ctx, cx - iconS / 2, y, iconS, motion, speed);
            y += iconS + 10;
        }

        String name = "VICTUS CLIENT";
        int tw = Math.round(Logo.titleWidth(textRenderer, name) * titleScale);
        final int ty = y;
        Compat.withScale(ctx, titleScale, () ->
                Logo.title(ctx, textRenderer, name, Math.round((cx - tw / 2f) / titleScale), Math.round(ty / titleScale), motion, speed));
        y += titleH + 4;

        String tag = br != null && !br.tagline.value.isBlank() ? br.tagline.value : "MINECRAFT CLIENT";
        UI.centered(ctx, textRenderer, tag, cx, y, UI.MUTED);
        y += 10;
        // animated accent underline
        int lineW = 56;
        UI.fill(ctx, cx - lineW / 2, y + 3, cx + lineW / 2, y + 4, UI.ACCENT_DIM);
        int seg = 14;
        int pos = (int) ((System.currentTimeMillis() / 18) % (lineW + seg)) - seg;
        int sx1 = Math.max(0, pos), sx2 = Math.min(lineW, pos + seg);
        if (sx2 > sx1) UI.fill(ctx, cx - lineW / 2 + sx1, y + 3, cx - lineW / 2 + sx2, y + 4, UI.ACCENT);
        y += 18;

        int bx = cx - BTN_W / 2;
        button(ctx, mx, my, bx, y, "Singleplayer", false, () -> client.setScreen(new SelectWorldScreen(this)));
        y += BTN_H + GAP;
        button(ctx, mx, my, bx, y, "Multiplayer", false, () -> client.setScreen(new MultiplayerScreen(this)));
        y += BTN_H + GAP;
        button(ctx, mx, my, bx, y, "Options", false, () -> client.setScreen(new OptionsScreen(this, client.options)));
        y += BTN_H + GAP;
        button(ctx, mx, my, bx, y, "Victus Mods", true, () -> client.setScreen(new ClickGUIScreen()));
        y += BTN_H + GAP + 8;

        // quit
        Hit q = hit(bx, y, BTN_W, 20, client::scheduleStop);
        boolean qh = q.in(mx, my);
        UI.card(ctx, q.x, q.y, q.w, q.h, qh ? UI.ACCENT_DIM : 0xAA0F0F13, qh ? UI.ACCENT : UI.ACCENT_DIM);
        UI.centered(ctx, textRenderer, "QUIT GAME", cx, y + 6, qh ? 0xFFFFFFFF : UI.ACCENT);

        // top-right quick links
        int tx = width - 8;
        tx -= chip(ctx, mx, my, tx, 8, "HUD Editor", () -> client.setScreen(new HudEditorScreen(this)));
        tx -= 5;
        chip(ctx, mx, my, tx, 8, "Profiles", () -> client.setScreen(new ProfilesScreen(this)));

        // footer
        UI.text(ctx, textRenderer, "Victus Client v" + MOD_VERSION, 6, height - 12, UI.MUTED);
        String mc = "Minecraft " + MC_VERSION;
        UI.text(ctx, textRenderer, mc, width - textRenderer.getWidth(mc) - 6, height - 12, UI.MUTED);
        UI.centered(ctx, textRenderer, "Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.",
                cx, height - 12, 0xFF4A4A55);

        UI.alpha = 1f;
    }

    private void button(DrawContext ctx, int mx, int my, int x, int y, String label, boolean primary, Runnable action) {
        Hit b = hit(x, y, BTN_W, BTN_H, action);
        boolean hov = b.in(mx, my);
        if (primary) {
            UI.rrect(ctx, x, y, BTN_W, BTN_H, hov ? UI.mix(UI.ACCENT, 0xFFFFFFFF, 0.18f) : UI.ACCENT);
            UI.centered(ctx, textRenderer, label, x + BTN_W / 2, y + (BTN_H - 8) / 2, 0xFFFFFFFF);
        } else {
            UI.card(ctx, x, y, BTN_W, BTN_H, hov ? 0xDD17171D : 0xB00F0F13, hov ? UI.ACCENT_DIM : UI.BORDER);
            if (hov) UI.fill(ctx, x + 1, y + 3, x + 3, y + BTN_H - 3, UI.ACCENT);
            UI.centered(ctx, textRenderer, label, x + BTN_W / 2, y + (BTN_H - 8) / 2, hov ? UI.TEXT : 0xFFD4D4D8);
        }
    }

    /** Right-aligned small pill; returns its width. */
    private int chip(DrawContext ctx, int mx, int my, int right, int y, String label, Runnable action) {
        int w = textRenderer.getWidth(label) + 14, h = 16, x = right - w;
        Hit b = hit(x, y, w, h, action);
        boolean hov = b.in(mx, my);
        UI.card(ctx, x, y, w, h, hov ? 0xDD17171D : 0xB00F0F13, hov ? UI.ACCENT_DIM : UI.BORDER);
        UI.centered(ctx, textRenderer, label, x + w / 2, y + 4, hov ? UI.TEXT : UI.MUTED);
        return w;
    }

    // ---- input -----------------------------------------------------------------------------

    @Override
    protected boolean onClick(double x, double y, int button) {
        if (button != 0) return false;
        for (Hit h : hits) {
            if (h.in(x, y) && h.left != null) { h.left.run(); return true; }
        }
        return false;
    }
}
