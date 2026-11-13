package com.victusclient.gui;

import com.victusclient.module.Colors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Procedural Victus logo (a "V" tile) and animated wordmark. No texture assets needed. */
public final class Logo {
    private Logo() {}

    public static void icon(DrawContext c, int x, int y, int s, String motion, float speed) {
        long t = System.currentTimeMillis();
        int tile = UI.ACCENT;
        if (motion.equals("Rainbow")) tile = Colors.rainbow(0f, speed);

        if (motion.equals("Pulse")) {
            double k = (Math.sin(t / (260.0 / Math.max(0.2f, speed))) + 1) / 2;
            int a = 0x18 + (int) (k * 0x48);
            int g = Math.max(2, s / 6);
            UI.rrect(c, x - g, y - g, s + 2 * g, s + 2 * g, Colors.withAlpha(tile, a));
        }
        UI.rrect(c, x, y, s, s, tile);

        // white "V" glyph
        int pad = Math.max(2, s / 6);
        int gx = x + pad, gy = y + pad, gw = s - 2 * pad, gh = s - 2 * pad;
        int th = Math.max(2, gw / 4);
        for (int r = 0; r < gh; r++) {
            float f = gh <= 1 ? 1f : r / (float) (gh - 1);
            int shift = Math.round((gw / 2f - th) * f);
            UI.fill(c, gx + shift, gy + r, gx + shift + th, gy + r + 1, 0xFFFFFFFF);
            UI.fill(c, gx + gw - shift - th, gy + r, gx + gw - shift, gy + r + 1, 0xFFFFFFFF);
        }

        if (motion.equals("Shimmer")) {
            int span = s * 3;
            int pos = (int) ((t * speed / 12.0) % span) - s;
            c.enableScissor(x, y, x + s, y + s);
            int bw = Math.max(3, s / 5);
            for (int r = 0; r < s; r++) {
                int bx = x + pos + (s - r) / 2;
                UI.fill(c, bx, y + r, bx + bw, y + r + 1, 0x66FFFFFF);
            }
            c.disableScissor();
        }
    }

    /** First word white, the rest in the accent colour. */
    private static boolean accentAt(String name, int i) {
        int sp = name.indexOf(' ');
        return sp >= 0 && i > sp;
    }

    private static Text glyph(String ch) { return Text.literal(ch).formatted(Formatting.BOLD); }

    public static int titleWidth(TextRenderer tr, String name) {
        int w = 0;
        for (int i = 0; i < name.length(); i++) w += tr.getWidth(glyph(String.valueOf(name.charAt(i))));
        return w;
    }

    public static void title(DrawContext c, TextRenderer tr, String name, int x, int y, String motion, float speed) {
        long t = System.currentTimeMillis();
        int n = name.length();
        int cx = x;
        for (int i = 0; i < n; i++) {
            String ch = String.valueOf(name.charAt(i));
            int base = accentAt(name, i) ? UI.ACCENT : UI.TEXT;
            int col = base, dy = 0;
            switch (motion) {
                case "Pulse" -> {
                    double k = (Math.sin(t / (260.0 / Math.max(0.2f, speed))) + 1) / 2;
                    if (base == UI.ACCENT) col = UI.mix(base, 0xFFFFFFFF, (float) (k * 0.55));
                }
                case "Shimmer" -> {
                    double sweep = ((t * speed / 90.0) % (n + 8)) - 4;
                    double k = Math.max(0, 1 - Math.abs(i - sweep) / 2.5);
                    col = UI.mix(base, base == UI.TEXT ? UI.ACCENT : 0xFFFFFFFF, (float) (k * 0.9));
                }
                case "Wave" -> dy = (int) Math.round(Math.sin(t / (210.0 / Math.max(0.2f, speed)) + i * 0.65) * 2);
                case "Rainbow" -> col = Colors.rainbow(i * 0.07f, speed);
                default -> { }
            }
            UI.text(c, tr, glyph(ch), cx, y + dy, col);
            cx += tr.getWidth(glyph(ch));
        }
    }
}
