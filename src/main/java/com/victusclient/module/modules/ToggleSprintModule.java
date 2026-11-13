package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.minecraft.client.MinecraftClient;

public class ToggleSprintModule extends Module {
    public ToggleSprintModule() { super("Toggle Sprint", Category.MOVEMENT); }

    @Override public void onTick(MinecraftClient mc) {
        var p = mc.player;
        if (p == null || mc.currentScreen != null) return;
        if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                && p.getHungerManager().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}
