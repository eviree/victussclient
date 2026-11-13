package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HungerModule extends HudModule {
    public HungerModule() { super("Hunger", 6, 330); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        var hm = mc.player.getHungerManager();
        box(ctx, mc, String.format("Food: %d | Sat: %.1f", hm.getFoodLevel(), hm.getSaturationLevel()));
    }
}
