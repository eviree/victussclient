package com.victusclient.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
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
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("victusclient", "main"));
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.victusclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
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

    /**
     * Minecraft 1.21.11 replaced getGraphicsMode() with graphics presets (getPreset()), which only exist on
     * 1.21.11 - not on 1.21.9/1.21.10 that this jar also covers - so the Fast/Fancy switch is skipped here.
     */
    public static void setFancyGraphics(GameOptions o, boolean fancy) { }

    public static String effectName(net.minecraft.entity.effect.StatusEffectInstance e) {
        return e.getEffectType().value().getName().getString();
    }
}
