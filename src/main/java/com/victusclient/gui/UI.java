package com.victusclient.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Victus theme: colours are mutable so Color Themes / Custom Themes can restyle the whole menu. */
public final class UI {
    public static int BG = 0xFF09090B;
    public static int SURFACE = 0xFF0F0F13;
    public static int SURFACE_HOVER = 0xFF17171D;
    public static int BORDER = 0xFF23232B;
    public static int TEXT = 0xFFFFFFFF;
    public static int MUTED = 0xFF8A8A96;
    public static int ACCENT = 0xFFE11D48;
    public static int ACCENT_DIM = 0xFF6B1226;
    public static int GREEN = 0xFF34D399;

    /** Global fade multiplier (0..1) used by menu open animations. */
    public static float alpha = 1f;
    /** 2 = rounded corners, 0 = square. */
    public static int radius = 2;

    private UI() {}

    public static int fade(int argb) {
        if (alpha >= 0.999f) return argb;
        int a = (int) (((argb >>> 24) & 0xFF) * Math.max(0f, alpha));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t), g = (int) (ag + (bg - ag) * t), bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    public static void fill(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        if (x2 <= x1 || y2 <= y1) return;
        c.fill(x1, y1, x2, y2, fade(col));
    }

    /** Filled rect with small rounded corners (non-overlapping so translucent fills stay even). */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        int r = Math.min(radius, Math.min(w, h) / 2);
        if (r <= 0) { fill(c, x, y, x + w, y + h, col); return; }
        if (r == 1) {
            fill(c, x + 1, y, x + w - 1, y + 1, col);
            fill(c, x, y + 1, x + w, y + h - 1, col);
            fill(c, x + 1, y + h - 1, x + w - 1, y + h, col);
            return;
        }
        fill(c, x + 2, y, x + w - 2, y + 1, col);
        fill(c, x + 1, y + 1, x + w - 1, y + 2, col);
        fill(c, x, y + 2, x + w, y + h - 2, col);
        fill(c, x + 1, y + h - 2, x + w - 1, y + h - 1, col);
        fill(c, x + 2, y + h - 1, x + w - 2, y + h, col);
    }

    /** Rounded card with 1px border. */
    public static void card(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        rrect(c, x, y, w, h, border);
        rrect(c, x + 1, y + 1, w - 2, h - 2, fill);
    }

    /** Plain 1px rectangle outline. */
    public static void frame(DrawContext c, int x, int y, int w, int h, int col) {
        fill(c, x, y, x + w, y + 1, col);
        fill(c, x, y + h - 1, x + w, y + h, col);
        fill(c, x, y, x + 1, y + h, col);
        fill(c, x + w - 1, y, x + w, y + h, col);
    }

    // Text (alpha < 4 would be treated as opaque by the text renderer, so skip those)
    public static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int col) {
        int f = fade(col);
        if (((f >>> 24) & 0xFF) < 4) return;
        c.drawText(tr, s, x, y, f, false);
    }

    public static void text(DrawContext c, TextRenderer tr, Text s, int x, int y, int col) {
        int f = fade(col);
        if (((f >>> 24) & 0xFF) < 4) return;
        c.drawText(tr, s, x, y, f, false);
    }

    public static void centered(DrawContext c, TextRenderer tr, String s, int cx, int y, int col) {
        text(c, tr, s, cx - tr.getWidth(s) / 2, y, col);
    }
}
