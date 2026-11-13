package com.victusclient;

import com.victusclient.compat.Compat;
import com.victusclient.config.Config;
import com.victusclient.gui.ClickGUIScreen;
import com.victusclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public class VictusClient implements ClientModInitializer {
    public static final String ID = "victusclient";
    public static final int ACCENT = 0xFF8B5CF6; // Victus purple
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        Config.load();
        com.victusclient.gui.Theme.apply();

        menuKey = Compat.registerMenuKey();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUIScreen());
            }
            ModuleManager.tick(mc);
        });

        Compat.registerHud(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            ModuleManager.render(ctx, mc);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}
