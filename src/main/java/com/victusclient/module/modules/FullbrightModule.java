package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullbrightModule extends Module {
    public FullbrightModule() { super("Fullbright", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (cur == null || cur.getDuration() < 400)
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 16000, 0, false, false));
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
