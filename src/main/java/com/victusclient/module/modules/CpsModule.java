package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends HudModule {
    private final Setting.Bool showRight = add(new Setting.Bool("Show Right Click", true));
    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private boolean lastL, lastR;

    public CpsModule() { super("CPS", 6, 26); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (mc.currentScreen == null) {
            if (l && !lastL) left.add(now);
            if (r && !lastR) right.add(now);
        }
        lastL = l; lastR = r;
        while (!left.isEmpty() && now - left.peekFirst() > 1000) left.pollFirst();
        while (!right.isEmpty() && now - right.peekFirst() > 1000) right.pollFirst();
        box(ctx, mc, showRight.value ? "CPS: " + left.size() + " | " + right.size() : "CPS: " + left.size());
    }
}
