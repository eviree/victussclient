package com.victusclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Victus startup popup. Wraps the vanilla splash overlay: the vanilla overlay keeps doing the real
 * resource loading, we draw an opaque branded card on top, hold it for 1 s after loading finishes
 * (like the vanilla splash does) and then fade it out into the Victus main menu.
 */
public class VictusLoadingOverlay extends Overlay {
    private static final AtomicBoolean CLAIMED = new AtomicBoolean(false);

    /** Only the very first splash overlay (game start) is branded; F3+T reloads keep vanilla behaviour. */
    public static boolean claim() { return CLAIMED.compareAndSet(false, true); }

    private final Overlay inner;
    private final long startMs = System.currentTimeMillis();
    private long doneMs = -1;

    public VictusLoadingOverlay(Overlay inner) { this.inner = inner; }

    public boolean pausesGame() { return inner.pausesGame(); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        inner.render(ctx, mx, my, delta);   // real loading logic + vanilla visuals (hidden below)

        long now = System.currentTimeMillis();
        if (doneMs < 0 && mc.currentScreen != null) doneMs = now;   // vanilla opens the menu when loading ends

        float a = 1f;
        if (doneMs >= 0) {
            float f = (now - doneMs) / 1000f;               // 0-1 s hold, 1-2 s fade
            float x = Math.max(0f, Math.min(1f, f - 1f));
            a = 1f - x * x;                                  // stays high a bit longer than vanilla's fade
        }
        if (a <= 0.003f) return;

        float prev = UI.alpha;
        try {
            Theme.apply();
            UI.alpha = a;
            int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
            draw(ctx, mc, w, h, now);
        } catch (RuntimeException ignored) {
            // A decorative startup effect must never prevent Minecraft from completing its own load.
        } finally {
            UI.alpha = prev;
        }
    }

    private void draw(DrawContext ctx, MinecraftClient mc, int w, int h, long now) {
        int cx = w / 2, cy = h / 2;
        UI.fill(ctx, 0, 0, w, h, UI.BG);
        ctx.fillGradient(0, 0, w, h, UI.fade(0x00000000), UI.fade(0x99000000));
        Fx.particles(ctx, w, h);

        int icon = 44;
        Logo.icon(ctx, cx - icon / 2, cy - 52, icon, "Pulse", 1f);

        String name = "VICTUS CLIENT";
        int nw = Logo.titleWidth(mc.textRenderer, name);
        Logo.title(ctx, mc.textRenderer, name, cx - nw / 2, cy + 4, "Shimmer", 1f);

        // indeterminate progress bar
        int bw = 140, bx = cx - bw / 2, by = cy + 24;
        UI.rrect(ctx, bx, by, bw, 4, UI.BORDER);
        if (doneMs >= 0) {
            UI.rrect(ctx, bx, by, bw, 4, UI.ACCENT);
        } else {
            int seg = 44;
            int pos = (int) ((now - startMs) / 7 % (bw + seg)) - seg;
            int x1 = Math.max(0, pos), x2 = Math.min(bw, pos + seg);
            if (x2 > x1) UI.rrect(ctx, bx + x1, by, x2 - x1, 4, UI.ACCENT);
        }

        String status;
        if (doneMs >= 0) status = "Ready";
        else {
            long ms = now - startMs;
            String[] steps = {"Loading resources", "Preparing Victus modules", "Almost there"};
            status = steps[(int) Math.min(steps.length - 1, ms / 1800)] + ".".repeat((int) (ms / 350 % 4));
        }
        UI.centered(ctx, mc.textRenderer, status, cx, by + 12, UI.MUTED);
        UI.centered(ctx, mc.textRenderer, "Victus Client", cx, h - 14, 0xFF4A4A55);
    }
}
