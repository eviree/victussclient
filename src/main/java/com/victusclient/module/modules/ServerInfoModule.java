package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import java.util.ArrayList;
import java.util.List;

public class ServerInfoModule extends HudModule {
    private final Setting.Bool address = add(new Setting.Bool("Show Address", true));
    private final Setting.Bool players = add(new Setting.Bool("Show Player Count", true));

    public ServerInfoModule() { super("Server Info", 6, 310); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        List<String> ls = new ArrayList<>();
        ServerInfo info = mc.getCurrentServerEntry();
        ClientPlayNetworkHandler nh = mc.getNetworkHandler();
        if (address.value) ls.add(info != null ? info.address : "Singleplayer");
        if (players.value && nh != null) ls.add("Players: " + nh.getPlayerList().size());
        lines(ctx, mc, ls);
    }
}
