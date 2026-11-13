package com.victusclient.module;

import com.victusclient.gui.UI;

/** Shared colour helpers for modules that draw their own things (crosshair, watermark). */
public final class Colors {
    public static final String[] NAMES = {"White", "Crimson", "Red", "Green", "Cyan", "Yellow", "Rainbow", "Theme"};
    private static final int[] FIXED = {0xFFFFFFFF, 0xFFFF4D6D, 0xFFFF5555, 0xFF55FF55, 0xFF55FFFF, 0xFFFFFF55};

    private Colors() {}

    public static int resolve(int index) {
        if (index == 6) return rainbow(0f, 1f);
        if (index == 7) return UI.ACCENT;
        return FIXED[Math.max(0, Math.min(index, FIXED.length - 1))];
    }

    /** Opaque rainbow colour; offset shifts the hue (0..1), speed scales the animation. */
    public static int rainbow(float offset, float speed) {
        float h = (((System.currentTimeMillis() * speed) % 3000L) / 3000f + offset) % 1f;
        if (h < 0) h += 1f;
        return 0xFF000000 | hsv(h, 0.65f, 1f);
    }

    public static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    public static int hsv(float h, float s, float v) {
        float r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
