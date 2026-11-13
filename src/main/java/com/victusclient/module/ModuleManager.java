package com.victusclient.module;

import com.victusclient.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        // HUD
        register(new FpsModule());
        register(new CpsModule());
        register(new CoordsModule());
        register(new KeystrokesModule());
        register(new PingModule());
        register(new SpeedModule());
        register(new DirectionModule());
        register(new ClockModule());
        register(new SessionTimerModule());
        register(new MemoryModule());
        register(new WorldInfoModule());
        register(new ServerInfoModule());
        register(new HungerModule());
        // PvP
        register(new ArmorStatusModule());
        register(new PotionStatusModule());
        register(new ComboModule());
        register(new ItemCounterModule());
        // Visual
        register(new FullbrightModule());
        register(new ZoomModule());
        register(new CrosshairModule());
        register(new ParticleSettingsModule());
        register(new ColorThemesModule());
        register(new CustomGuiModule());
        // Performance
        register(new PerformancePresetsModule());
        register(new RenderDistanceModule());
        // Movement
        register(new ToggleSprintModule());
        // Customize
        register(new CustomThemesModule());
        register(new MenuAnimationsModule());
        register(new BrandingModule());

        get("FPS").setEnabled(true);
        get("CPS").setEnabled(true);
        get("Color Themes").setEnabled(true);
        get("Menu Animations").setEnabled(true);
    }

    public static void register(Module m) { MODULES.add(m); }
    public static List<Module> all() { return MODULES; }

    private static final java.util.Map<Class<?>, Module> BY_CLASS = new java.util.HashMap<>();

    /** Find the registered module of the given class (cached). */
    public static <T extends Module> T of(Class<T> cls) {
        Module cached = BY_CLASS.get(cls);
        if (cached != null) return cls.cast(cached);
        for (Module m : MODULES) {
            if (cls.isInstance(m)) { BY_CLASS.put(cls, m); return cls.cast(m); }
        }
        return null;
    }

    public static Module get(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    private static final java.util.Map<Module, Boolean> KEY_DOWN = new java.util.HashMap<>();

    public static void tick(MinecraftClient mc) {
        // keybind editor: toggle modules on key press (only while no screen is open)
        boolean free = mc.currentScreen == null && mc.getWindow() != null;
        for (Module m : MODULES) {
            if (!m.bind.isBound()) { KEY_DOWN.put(m, false); continue; }
            boolean down = free && org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(), m.bind.key)
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            if (down && !KEY_DOWN.getOrDefault(m, false)) m.toggle();
            KEY_DOWN.put(m, down || (!free && KEY_DOWN.getOrDefault(m, false)));
        }
        for (Module m : MODULES) if (m.enabled) m.onTick(mc);
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        for (Module m : MODULES) if (m.enabled) m.onRender(ctx, mc);
    }
}
