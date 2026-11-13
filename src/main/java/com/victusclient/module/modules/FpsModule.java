package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class FpsModule extends HudModule {
    private final Setting.Bool stats = add(new Setting.Bool("Show Avg / Min / Max", false));
    private final Deque<Integer> samples = new ArrayDeque<>();
    private long lastSample;

    public FpsModule() { super("FPS", 6, 6); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        int fps = mc.getCurrentFps();
        if (!stats.value) { box(ctx, mc, "FPS: " + fps); return; }

        long now = System.currentTimeMillis();
        if (now - lastSample >= 100) {
            lastSample = now;
            samples.addLast(fps);
            while (samples.size() > 100) samples.pollFirst();   // ~10 seconds
        }
        int min = Integer.MAX_VALUE, max = 0;
        long sum = 0;
        for (int s : samples) { min = Math.min(min, s); max = Math.max(max, s); sum += s; }
        int avg = samples.isEmpty() ? fps : (int) (sum / samples.size());
        if (samples.isEmpty()) min = fps;

        List<String> ls = new ArrayList<>();
        ls.add("FPS: " + fps);
        ls.add("Avg " + avg + "  Min " + min + "  Max " + Math.max(max, fps));
        lines(ctx, mc, ls);
    }
}
