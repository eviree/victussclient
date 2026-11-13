package com.victusclient.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;

public final class Compat {
    private Compat() {}

    /** Set by Custom Crosshair. Vanilla crosshair can only be hidden on 1.21.6+ (HUD element API). */
    public static volatile boolean hideCrosshair;
    public static final boolean CAN_HIDE_CROSSHAIR = false;

    public static KeyBinding registerMenuKey() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.victusclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.victusclient"));
    }

    public static void registerHud(Consumer<DrawContext> renderer) {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> renderer.accept(ctx));
    }

    /** Runs a draw call with everything scaled by s (draw at x/s, y/s). */
    public static void withScale(DrawContext ctx, float s, Runnable draw) {
        var m = ctx.getMatrices();
        m.push();
        m.scale(s, s, 1f);
        draw.run();
        m.pop();
    }

    /** Fast/Fancy graphics switch. */
    public static void setFancyGraphics(GameOptions o, boolean fancy) {
        o.getGraphicsMode().setValue(fancy ? GraphicsMode.FANCY : GraphicsMode.FAST);
    }

    public static String effectName(net.minecraft.entity.effect.StatusEffectInstance e) {
        return e.getEffectType().getName().getString();
    }
}
