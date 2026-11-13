package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class DirectionModule extends HudModule {
    public DirectionModule() { super("Direction", 6, 190); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
        String[] names = {"S (+Z)", "SW", "W (-X)", "NW", "N (-Z)", "NE", "E (+X)", "SE"};
        // yaw 0 = south, 90 = west, 180/-180 = north, -90 = east
        int dir = Math.floorMod(Math.round(yaw / 45f), 8);
        box(ctx, mc, "Facing: " + names[dir] + "  " + (int) yaw + "\u00B0");
    }
}
