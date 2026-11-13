package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class WorldInfoModule extends HudModule {
    private final Setting.Bool biome = add(new Setting.Bool("Biome", true));
    private final Setting.Bool light = add(new Setting.Bool("Light Level", true));
    private final Setting.Bool day = add(new Setting.Bool("Day Count", true));

    public WorldInfoModule() { super("World Info", 6, 270); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = mc.player.getBlockPos();
        List<String> ls = new ArrayList<>();
        if (biome.value) {
            String name = mc.world.getBiome(pos).getKey().map(k -> k.getValue().getPath()).orElse("unknown");
            ls.add("Biome: " + name.replace('_', ' '));
        }
        if (light.value) ls.add("Light: " + mc.world.getLightLevel(pos));
        if (day.value) ls.add("Day: " + (mc.world.getTimeOfDay() / 24000L));
        lines(ctx, mc, ls);
    }
}
