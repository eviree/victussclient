package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import java.util.ArrayList;
import java.util.List;

public class PotionStatusModule extends HudModule {
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public PotionStatusModule() { super("Potion Status", Category.PVP, 6, 130); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        List<String> ls = new ArrayList<>();
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String name = com.victusclient.compat.Compat.effectName(e);
            int amp = e.getAmplifier();
            String lvl = amp < ROMAN.length ? ROMAN[amp] : String.valueOf(amp + 1);
            int ticks = e.getDuration();
            String time = ticks < 0 ? "**:**" : String.format("%d:%02d", ticks / 1200, (ticks / 20) % 60);
            ls.add(name + " " + lvl + "  " + time);
        }
        lines(ctx, mc, ls);
    }
}
