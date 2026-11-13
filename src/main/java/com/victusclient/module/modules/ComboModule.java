package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/** Counts your consecutive hits. Resets when you get hit or stop hitting for 3s. */
public class ComboModule extends HudModule {
    private int combo, best, prevHurt;
    private long lastHit;

    public ComboModule() {
        super("Combo", Category.PVP, 6, 110);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (enabled && player == MinecraftClient.getInstance().player && entity instanceof LivingEntity) {
                combo++;
                best = Math.max(best, combo);
                lastHit = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });
    }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        int hurt = mc.player.hurtTime;
        if (hurt > 0 && prevHurt == 0) combo = 0;
        prevHurt = hurt;
        if (combo > 0 && System.currentTimeMillis() - lastHit > 3000) combo = 0;
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        box(ctx, mc, "Combo: " + combo + "  (Best " + best + ")");
    }
}
