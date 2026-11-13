package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Hold the zoom key (default C, rebindable) to zoom. */
public class ZoomModule extends Module {
    private final Setting.Key zoomKey = add(new Setting.Key("Zoom Key", GLFW.GLFW_KEY_C));
    private final Setting.Slider zoomFov = add(new Setting.Slider("Zoom FOV", 30, 30, 70, 1));
    private final Setting.Bool smooth = add(new Setting.Bool("Smooth Zoom", true));
    private boolean zooming;
    private int savedFov;
    private double current;

    public ZoomModule() { super("Zoom", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        boolean held = zoomKey.isBound() && mc.currentScreen == null
                && GLFW.glfwGetKey(mc.getWindow().getHandle(), zoomKey.key) == GLFW.GLFW_PRESS;
        if (held && !zooming) {
            savedFov = mc.options.getFov().getValue();
            current = savedFov;
            zooming = true;
        }
        if (held) {
            double target = zoomFov.value;
            current = smooth.value ? current + (target - current) * 0.35 : target;
            mc.options.getFov().setValue((int) Math.round(current));
        } else if (zooming) {
            if (smooth.value && Math.abs(current - savedFov) > 1.5) {
                current += (savedFov - current) * 0.35;
                mc.options.getFov().setValue((int) Math.round(current));
            } else {
                restore(mc);
            }
        }
    }

    private void restore(MinecraftClient mc) {
        mc.options.getFov().setValue(savedFov);
        zooming = false;
    }

    @Override public void onDisable() {
        if (zooming) restore(MinecraftClient.getInstance());
    }
}
