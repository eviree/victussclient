package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class MemoryModule extends HudModule {
    private final Setting.Mode display = add(new Setting.Mode("Display", 0, "Used / Max", "Percent", "Used Only"));

    public MemoryModule() { super("Memory", 6, 250); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        Runtime r = Runtime.getRuntime();
        long used = (r.totalMemory() - r.freeMemory()) / 1048576L;
        long max = r.maxMemory() / 1048576L;
        String s = switch (display.index) {
            case 1 -> "RAM: " + (max == 0 ? 0 : used * 100 / max) + "%";
            case 2 -> "RAM: " + used + " MB";
            default -> "RAM: " + used + " / " + max + " MB";
        };
        box(ctx, mc, s);
    }
}
