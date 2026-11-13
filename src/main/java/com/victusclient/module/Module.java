package com.victusclient.module;

import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final Category category;
    public boolean enabled;
    public boolean favorite;
    public final List<Setting> settings = new ArrayList<>();
    /** Keybind editor: pressing this key (no screen open) toggles the module. */
    public final Setting.Key bind = add(new Setting.Key("Toggle Key", -1));

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (enabled == e) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onRender(DrawContext ctx, MinecraftClient mc) {}
}
