package com.victusclient.gui;

import com.victusclient.compat.VScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Branded in-game pause menu. Vanilla world/network logic is left untouched. */
public final class VictusPauseScreen extends VScreen {
    private final List<Hit> hits = new ArrayList<>();

    public VictusPauseScreen() { super(Text.literal("Victus Pause")); }
    @Override public boolean shouldPause() { return true; }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        Theme.apply();
        hits.clear();
        int w = width, h = height, cx = w / 2;
        ctx.fill(0, 0, w, h, 0x99060710);
        Fx.vignette(ctx, w, h);
        Fx.particles(ctx, w, h);

        int panelW = Math.min(430, w - 28), panelH = 330;
        int x = (w - panelW) / 2, y = (h - panelH) / 2;
        UI.card(ctx, x, y, panelW, panelH, 0xF20C0B16, UI.ACCENT_DIM);
        UI.fill(ctx, x + 2, y + 2, x + panelW - 2, y + 4, UI.ACCENT);

        int icon = 52;
        Logo.icon(ctx, cx - icon / 2, y + 18, icon, "Pulse", 1f);
        Logo.title(ctx, textRenderer, "VICTUS CLIENT", cx - Logo.titleWidth(textRenderer, "VICTUS CLIENT") / 2, y + 78, "Shimmer", 1f);
        UI.centered(ctx, textRenderer, "GAME PAUSED  •  TAKE YOUR TIME", cx, y + 98, UI.MUTED);

        int gap = 8, bw = (panelW - 54) / 2, bh = 34;
        int left = x + 18, right = left + bw + gap, by = y + 132;
        button(ctx, mx, my, left, by, bw, bh, "RESUME GAME", true, () -> client.setScreen(null));
        button(ctx, mx, my, right, by, bw, bh, "OPTIONS", false, () -> client.setScreen(new OptionsScreen(this, client.options)));
        by += bh + 9;
        button(ctx, mx, my, left, by, bw, bh, "VICTUS HUD / MODS", false, () -> client.setScreen(new ClickGUIScreen()));
        button(ctx, mx, my, right, by, bw, bh, "HUD EDITOR", false, () -> client.setScreen(new HudEditorScreen(this)));
        by += bh + 9;
        button(ctx, mx, my, left, by, panelW - 36, bh, "SAVE & RETURN TO TITLE", false, () -> {
            client.disconnect();
            client.setScreen(new VictusTitleScreen());
        });

        UI.centered(ctx, textRenderer, "VICTUS CLIENT  •  " + net.minecraft.SharedConstants.getGameVersion().getName(), cx, y + panelH - 22, UI.MUTED);
    }

    private void button(DrawContext ctx, int mx, int my, int x, int y, int w, int h, String label, boolean primary, Runnable action) {
        Hit hit = new Hit(x, y, w, h); hit.left = action; hits.add(hit);
        boolean hover = hit.in(mx, my);
        int fill = primary ? (hover ? UI.mix(UI.ACCENT, 0xFFFFFFFF, .18f) : UI.ACCENT) : (hover ? UI.SURFACE_HOVER : UI.SURFACE);
        UI.card(ctx, x, y, w, h, fill, primary ? UI.ACCENT : (hover ? UI.ACCENT : UI.BORDER));
        UI.centered(ctx, textRenderer, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override protected boolean onClick(double x, double y, int button) {
        if (button != 0) return false;
        for (Hit hit : hits) if (hit.in(x, y) && hit.left != null) { hit.left.run(); return true; }
        return false;
    }
}
