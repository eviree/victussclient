package com.victusclient.module.modules;

import com.victusclient.compat.Compat;
import com.victusclient.module.Category;
import com.victusclient.module.Colors;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Custom crosshair drawn on the HUD. Hides the vanilla one where the loader API allows (1.21.6+). */
public class CrosshairModule extends Module {
    private final Setting.Mode style = add(new Setting.Mode("Style", 0, "Cross", "Plus", "Dot", "Circle", "T-Shape"));
    private final Setting.Slider size = add(new Setting.Slider("Size", 6, 2, 30, 1));
    private final Setting.Slider thickness = add(new Setting.Slider("Thickness", 1, 1, 8, 1));
    private final Setting.Slider gap = add(new Setting.Slider("Gap", 3, 0, 14, 1));
    private final Setting.Mode color = add(new Setting.Mode("Color", 0, Colors.NAMES));
    private final Setting.Slider opacity = add(new Setting.Slider("Opacity", 100, 20, 100, 5));
    private final Setting.Bool outline = add(new Setting.Bool("Outline", true));
    private final Setting.Bool centerDot = add(new Setting.Bool("Center Dot", false));
    private final Setting.Bool dynamic = add(new Setting.Bool("Dynamic Gap", false));
    private final Setting.Bool hideVanilla = add(new Setting.Bool("Hide Vanilla (1.21.6+)", true));

    private float spread;

    public CrosshairModule() { super("Custom Crosshair", Category.VISUAL); }

    @Override public void onDisable() { Compat.hideCrosshair = false; }

    @Override public void onRender(DrawContext ctx, MinecraftClient mc) {
        boolean visible = mc.currentScreen == null && mc.player != null && mc.options.getPerspective().isFirstPerson();
        Compat.hideCrosshair = hideVanilla.value && visible;
        if (!visible) return;

        int cx = mc.getWindow().getScaledWidth() / 2;
        int cy = mc.getWindow().getScaledHeight() / 2;
        int t = (int) thickness.value;
        int len = (int) size.value;

        float target = 0f;
        if (dynamic.value) {
            double v = mc.player.getVelocity().horizontalLength();
            target = (float) Math.min(8.0, v * 24.0) + (mc.player.isSprinting() ? 2f : 0f);
        }
        spread += (target - spread) * 0.2f;
        int g = (int) gap.value + Math.round(spread);

        int base = color.index == 6 ? Colors.rainbow(0f, 1f) : Colors.resolve(color.index);
        int col = Colors.withAlpha(base, (int) (opacity.value * 2.55));
        int out = Colors.withAlpha(0x000000, (int) (opacity.value * 2.55 * 0.75));
        int half = t / 2;

        switch (style.get()) {
            case "Plus" -> {
                bar(ctx, cx - len, cy - half, 2 * len + 1, t, col, out);
                bar(ctx, cx - half, cy - len, t, 2 * len + 1, col, out);
            }
            case "Dot" -> {
                int d = Math.max(t, len / 2);
                bar(ctx, cx - d / 2, cy - d / 2, d, d, col, out);
            }
            case "Circle" -> circle(ctx, cx, cy, len + g, t, col, out);
            case "T-Shape" -> {
                bar(ctx, cx - g - len, cy - half, len, t, col, out);
                bar(ctx, cx + g + 1, cy - half, len, t, col, out);
                bar(ctx, cx - half, cy + g + 1, t, len, col, out);
            }
            default -> {
                bar(ctx, cx - g - len, cy - half, len, t, col, out);
                bar(ctx, cx + g + 1, cy - half, len, t, col, out);
                bar(ctx, cx - half, cy - g - len, t, len, col, out);
                bar(ctx, cx - half, cy + g + 1, t, len, col, out);
            }
        }
        if (centerDot.value && !style.get().equals("Dot")) {
            bar(ctx, cx - half, cy - half, t, t, col, out);
        }
    }

    private void bar(DrawContext ctx, int x, int y, int w, int h, int col, int out) {
        if (outline.value) ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, out);
        ctx.fill(x, y, x + w, y + h, col);
    }

    private void circle(DrawContext ctx, int cx, int cy, int r, int t, int col, int out) {
        int steps = Math.max(24, r * 8);
        if (outline.value) {
            for (int i = 0; i < steps; i++) {
                double a = i * (Math.PI * 2 / steps);
                int px = cx + (int) Math.round(Math.cos(a) * r) - t / 2;
                int py = cy + (int) Math.round(Math.sin(a) * r) - t / 2;
                ctx.fill(px - 1, py - 1, px + t + 1, py + t + 1, out);
            }
        }
        for (int i = 0; i < steps; i++) {
            double a = i * (Math.PI * 2 / steps);
            int px = cx + (int) Math.round(Math.cos(a) * r) - t / 2;
            int py = cy + (int) Math.round(Math.sin(a) * r) - t / 2;
            ctx.fill(px, py, px + t, py + t, col);
        }
    }
}
