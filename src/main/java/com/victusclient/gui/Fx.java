package com.victusclient.gui;

import com.victusclient.module.Colors;
import net.minecraft.client.gui.DrawContext;

/** Small procedural effects shared by the Victus title screen and loading popup (no textures needed). */
final class Fx {
    private Fx() {}

    /** Slowly rising accent-coloured dust. */
    static void particles(DrawContext c, int w, int h) {
        long t = System.currentTimeMillis();
        int W = Math.max(1, w), H = Math.max(1, h);
        for (int i = 0; i < 40; i++) {
            double sp = 0.006 + (i % 7) * 0.002;
            int x = (int) ((i * 53L) % W + Math.sin(t / 2600.0 + i) * 6);
            int y = H - (int) (((t * sp) + i * 37L) % (H + 10));
            int s = (i % 3 == 0) ? 2 : 1;
            int a = 0x16 + (i % 4) * 0x0C;
            UI.fill(c, x, y, x + s, y + s, Colors.withAlpha(UI.ACCENT, a));
        }
    }

    /** Dark top/bottom shading so text stays readable on top of any background. */
    static void vignette(DrawContext c, int w, int h) {
        c.fillGradient(0, 0, w, h / 3, 0xB0000000, 0x00000000);
        c.fillGradient(0, h - h / 3, w, h, 0x00000000, 0xC0000000);
    }
}
