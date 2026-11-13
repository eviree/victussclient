package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.module.OptionUtil;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Forces the particle level while enabled and restores your previous level when switched off. */
public class ParticleSettingsModule extends Module {
    // ordinals of the particle enum: 0 = All, 1 = Decreased, 2 = Minimal (same on every supported version)
    private final Setting.Mode level = add(new Setting.Mode("Particle Level", 2, "All", "Decreased", "Minimal"));
    private Object saved;

    public ParticleSettingsModule() { super("Particle Settings", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.options == null) return;
        var opt = mc.options.getParticles();
        if (saved == null) saved = opt.getValue();
        OptionUtil.setByOrdinal(opt, level.index);
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (saved != null && mc != null && mc.options != null) OptionUtil.restore(mc.options.getParticles(), saved);
        saved = null;
    }
}
