package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockModule extends HudModule {
    private final Setting.Bool h24 = add(new Setting.Bool("24 Hour", false));
    private static final DateTimeFormatter F24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter F12 = DateTimeFormatter.ofPattern("hh:mm a");

    public ClockModule() { super("Clock", 6, 210); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        box(ctx, mc, LocalTime.now().format(h24.value ? F24 : F12));
    }
}
