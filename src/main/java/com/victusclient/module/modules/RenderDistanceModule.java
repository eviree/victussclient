package com.victusclient.module.modules;

import com.victusclient.config.Config;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Render / simulation / entity distance sliders, with optional FPS-based auto adjust. */
public class RenderDistanceModule extends Module {
    private final Setting.Slider render = add(new Setting.Slider("Render Distance", 12, 2, 32, 1));
    private final Setting.Slider simulation = add(new Setting.Slider("Simulation Distance", 10, 5, 32, 1));
    private final Setting.Slider entity = add(new Setting.Slider("Entity Distance %", 100, 50, 300, 10));
    private final Setting.Bool auto = add(new Setting.Bool("Auto Adjust", false));
    private final Setting.Slider targetFps = add(new Setting.Slider("Target FPS", 60, 30, 240, 10));
    private final Setting.Slider autoMin = add(new Setting.Slider("Auto Minimum", 6, 4, 16, 1));

    private int lastRd = -1, lastSd = -1, lastEd = -1, cur = -1, ticks;
    private boolean pending;

    public RenderDistanceModule() { super("Render Distance", Category.PERFORMANCE); }

    @Override public void onEnable() { if (!Config.loading) pending = true; }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.options == null) return;
        int rd = (int) render.value, sd = (int) simulation.value, ed = (int) entity.value;

        if (lastRd == -1) { lastRd = rd; lastSd = sd; lastEd = ed; cur = rd; }
        if (rd != lastRd) { lastRd = rd; cur = rd; pending = true; }
        if (sd != lastSd) { lastSd = sd; pending = true; }
        if (ed != lastEd) { lastEd = ed; pending = true; }

        if (auto.value) {
            if (++ticks >= 40) {
                ticks = 0;
                int fps = mc.getCurrentFps();
                int floor = Math.min((int) autoMin.value, rd);
                if (fps < targetFps.value * 0.9 && cur > floor) { cur--; pending = true; }
                else if (fps > targetFps.value * 1.5 && cur < rd) { cur++; pending = true; }
            }
        } else if (cur != rd) {
            cur = rd; pending = true;
        }

        if (!pending) return;
        pending = false;
        mc.options.getViewDistance().setValue(cur);
        mc.options.getSimulationDistance().setValue(sd);
        mc.options.getEntityDistanceScaling().setValue(ed / 100.0);
    }
}
