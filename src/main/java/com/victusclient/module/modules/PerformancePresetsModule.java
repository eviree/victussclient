package com.victusclient.module.modules;

import com.victusclient.compat.Compat;
import com.victusclient.config.Config;
import com.victusclient.module.OptionUtil;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;

/**
 * One-click video setting bundles. Applied when you enable the module or change the preset
 * (not on every launch), so your manual Video Settings changes are never silently overridden.
 */
public class PerformancePresetsModule extends Module {
    public final Setting.Mode preset = add(new Setting.Mode("Preset", 2, "Potato", "Low", "Balanced", "High", "Quality"));

    private int lastIndex = -1;
    private boolean pending;

    public PerformancePresetsModule() { super("Performance Presets", Category.PERFORMANCE); }

    @Override public void onEnable() { if (!Config.loading) pending = true; }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.options == null) return;
        if (lastIndex == -1) lastIndex = preset.index;
        if (preset.index != lastIndex) { lastIndex = preset.index; pending = true; }
        if (!pending) return;
        pending = false;
        apply(mc.options, preset.index);
    }

    private static void apply(GameOptions o, int p) {
        //                         view sim  particles(0=all,1=less,2=min) entity  blend clouds                       shadows fancy
        switch (p) {
            case 0 -> set(o, 6, 5, 2, 0.5, 0, CloudRenderMode.OFF, false, false);
            case 1 -> set(o, 8, 6, 1, 0.75, 1, CloudRenderMode.OFF, false, false);
            case 2 -> set(o, 12, 10, 1, 1.0, 3, CloudRenderMode.FAST, true, true);
            case 3 -> set(o, 16, 12, 0, 1.0, 5, CloudRenderMode.FANCY, true, true);
            default -> set(o, 24, 16, 0, 1.5, 7, CloudRenderMode.FANCY, true, true);
        }
    }

    private static void set(GameOptions o, int view, int sim, int particles, double entity, int blend,
                            CloudRenderMode clouds, boolean shadows, boolean fancy) {
        o.getViewDistance().setValue(view);
        o.getSimulationDistance().setValue(sim);
        OptionUtil.setByOrdinal(o.getParticles(), particles);
        o.getEntityDistanceScaling().setValue(entity);
        o.getBiomeBlendRadius().setValue(blend);
        o.getCloudRenderMode().setValue(clouds);
        o.getEntityShadows().setValue(shadows);
        Compat.setFancyGraphics(o, fancy);
    }
}
