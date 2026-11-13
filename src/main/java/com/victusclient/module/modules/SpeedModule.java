package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SpeedModule extends HudModule {
    private double lastX = Double.NaN, lastZ, speed;

    public SpeedModule() { super("Speed", 6, 170); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        double x = mc.player.getX(), z = mc.player.getZ();
        if (!Double.isNaN(lastX)) speed = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ)) * 20.0;
        lastX = x; lastZ = z;
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        box(ctx, mc, String.format("Speed: %.1f b/s", speed));
    }
}
