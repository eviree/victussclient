package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public class PingModule extends HudModule {
    public PingModule() { super("Ping", 6, 90); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        ClientPlayNetworkHandler nh = mc.getNetworkHandler();
        if (mc.player == null || nh == null || mc.isInSingleplayer()) { box(ctx, mc, "Ping: --"); return; }
        PlayerListEntry e = nh.getPlayerListEntry(mc.player.getUuid());
        box(ctx, mc, e == null ? "Ping: --" : "Ping: " + e.getLatency() + " ms");
    }
}
