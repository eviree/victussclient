package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesModule extends HudModule {
    private final Setting.Bool showSpace = add(new Setting.Bool("Show Space", true));
    private final Setting.Bool showMouse = add(new Setting.Bool("Show Mouse Buttons", true));

    public KeystrokesModule() { super("Keystrokes", 6, 70); w = 64; h = 42; }

    private void key(DrawContext ctx, MinecraftClient mc, int kx, int ky, int kw, int kh, String label, KeyBinding kb) {
        boolean down = kb.isPressed();
        if (background.value || down) ctx.fill(kx, ky, kx + kw, ky + kh, down ? 0xA0FFFFFF : bgColor());
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawText(mc.textRenderer, label, kx + (kw - tw) / 2, ky + (kh - 8) / 2, down ? 0xFF000000 : textColor(), shadow.value && !down);
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        w = 64; h = 42;
        key(ctx, mc, x + 22, y, 20, 20, "W", mc.options.forwardKey);
        key(ctx, mc, x, y + 22, 20, 20, "A", mc.options.leftKey);
        key(ctx, mc, x + 22, y + 22, 20, 20, "S", mc.options.backKey);
        key(ctx, mc, x + 44, y + 22, 20, 20, "D", mc.options.rightKey);
        if (showSpace.value) {
            key(ctx, mc, x, y + h + 2, 64, 12, "SPACE", mc.options.jumpKey);
            h += 14;
        }
        if (showMouse.value) {
            key(ctx, mc, x, y + h + 2, 31, 18, "LMB", mc.options.attackKey);
            key(ctx, mc, x + 33, y + h + 2, 31, 18, "RMB", mc.options.useKey);
            h += 20;
        }
    }
}
