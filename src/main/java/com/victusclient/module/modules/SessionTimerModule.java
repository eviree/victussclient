package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SessionTimerModule extends HudModule {
    private final long start = System.currentTimeMillis();

    public SessionTimerModule() { super("Session Timer", 6, 230); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        long s = (System.currentTimeMillis() - start) / 1000;
        box(ctx, mc, String.format("Session: %02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60));
    }
}
