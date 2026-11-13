package com.victusclient.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;

public final class Compat {
    private Compat() {}

    /** Set by Custom Crosshair; read by the replaced vanilla crosshair element below. */
    public static volatile boolean hideCrosshair;
    public static final boolean CAN_HIDE_CROSSHAIR = true;

    public static KeyBinding registerMenuKey() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.victusclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.victusclient"));
    }

    public static void registerHud(Consumer<DrawContext> renderer) {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of("victusclient", "hud"),
                (ctx, tickCounter) -> renderer.accept(ctx));
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tickCounter) -> {
            if (!hideCrosshair) original.render(ctx, tickCounter);
        });
    }

    /** Runs a draw call with everything scaled by s (draw at x/s, y/s). */
    public static void withScale(DrawContext ctx, float s, Runnable draw) {
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.scale(s, s);
        draw.run();
        m.popMatrix();
    }

    /** Fast/Fancy graphics switch. */
    public static void setFancyGraphics(GameOptions o, boolean fancy) {
        o.getGraphicsMode().setValue(fancy ? GraphicsMode.FANCY : GraphicsMode.FAST);
    }

    public static String effectName(net.minecraft.entity.effect.StatusEffectInstance e) {
        return e.getEffectType().value().getName().getString();
    }
}
