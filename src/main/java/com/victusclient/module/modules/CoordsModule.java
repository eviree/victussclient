package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CoordsModule extends HudModule {
    private final Setting.Bool decimals = add(new Setting.Bool("Decimals", false));
    public CoordsModule() { super("Coordinates", 6, 46); }
    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        String s = decimals.value
                ? String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
                : String.format("XYZ: %d / %d / %d", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ());
        box(ctx, mc, s);
    }
}
