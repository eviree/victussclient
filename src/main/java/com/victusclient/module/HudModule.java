package com.victusclient.module;

import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public abstract class HudModule extends Module {
    public int x, y, w = 60, h = 16;
    /** Default position, used by the HUD editor's reset key. */
    public final int defX, defY;
    protected final Setting.Bool background = add(new Setting.Bool("Background", true));
    protected final Setting.Slider opacity = add(new Setting.Slider("Opacity", 55, 0, 100, 5));
    protected final Setting.Bool shadow = add(new Setting.Bool("Text Shadow", true));
    protected final Setting.Mode color = add(new Setting.Mode("Color", 0,
            "White", "Crimson", "Red", "Green", "Cyan", "Yellow", "Rainbow", "Theme"));
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF4D6D, 0xFFFF5555, 0xFF55FF55, 0xFF55FFFF, 0xFFFFFF55};

    protected HudModule(String name, int x, int y) { this(name, Category.HUD, x, y); }

    protected HudModule(String name, Category category, int x, int y) {
        super(name, category);
        this.x = x; this.y = y;
        this.defX = x; this.defY = y;
    }

    protected int bgColor() { return (((int) (opacity.value * 2.55)) & 0xFF) << 24; }

    protected int textColor() {
        if (color.get().equals("Theme")) return com.victusclient.gui.UI.ACCENT;
        if (color.get().equals("Rainbow")) return 0xFF000000 | hsv((System.currentTimeMillis() % 3000) / 3000f, 0.65f, 1f);
        return COLORS[Math.min(color.index, COLORS.length - 1)];
    }

    private static int hsv(float h, float s, float v) {
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

    protected abstract void draw(DrawContext ctx, MinecraftClient mc);

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) { draw(ctx, mc); }

    /** One-line text box. Updates w/h so the HUD editor knows the size. */
    protected void box(DrawContext ctx, MinecraftClient mc, String text) {
        w = mc.textRenderer.getWidth(text) + 10;
        h = 16;
        if (background.value) ctx.fill(x, y, x + w, y + h, bgColor());
        ctx.drawText(mc.textRenderer, text, x + 5, y + 4, textColor(), shadow.value);
    }

    /** Multi-line text box. */
    protected void lines(DrawContext ctx, MinecraftClient mc, List<String> ls) {
        if (ls.isEmpty()) { w = 60; h = 16; return; }
        int maxW = 0;
        for (String s : ls) maxW = Math.max(maxW, mc.textRenderer.getWidth(s));
        w = maxW + 10;
        h = ls.size() * 11 + 5;
        if (background.value) ctx.fill(x, y, x + w, y + h, bgColor());
        int yy = y + 4;
        for (String s : ls) {
            ctx.drawText(mc.textRenderer, s, x + 5, yy, textColor(), shadow.value);
            yy += 11;
        }
    }
}
