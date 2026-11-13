package com.victusclient.gui;

import com.victusclient.compat.VScreen;
import com.victusclient.config.Config;
import com.victusclient.module.HudModule;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** HUD Editor: drag elements with snapping to edges / centre / other elements, nudge with arrows. */
public class HudEditorScreen extends VScreen {
    private static boolean snap = true;
    private static final int SNAP_DIST = 5;

    private final Screen parent;
    private HudModule dragged, selected;
    private int offX, offY;
    private int guideX = -1, guideY = -1;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override public boolean shouldPause() { return false; }

    private List<HudModule> active() {
        List<HudModule> out = new ArrayList<>();
        for (Module m : ModuleManager.all()) if (m instanceof HudModule h && h.enabled) out.add(h);
        return out;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        UI.fill(ctx, 0, 0, width, height, 0x80000000);
        String t = "HUD EDITOR";
        UI.text(ctx, textRenderer, Text.literal(t).formatted(Formatting.BOLD), width / 2 - textRenderer.getWidth(t) / 2, 12, UI.ACCENT);
        String sub = "Drag to move  -  Arrows nudge  -  G snap: " + (snap ? "ON" : "OFF") + "  -  R reset  -  RShift / Esc finish";
        UI.text(ctx, textRenderer, sub, width / 2 - textRenderer.getWidth(sub) / 2, 26, UI.MUTED);

        if (dragged != null && snap) {
            UI.fill(ctx, width / 2, 0, width / 2 + 1, height, 0x26FFFFFF);
            UI.fill(ctx, 0, height / 2, width, height / 2 + 1, 0x26FFFFFF);
        }
        if (guideX >= 0) UI.fill(ctx, guideX, 0, guideX + 1, height, UI.ACCENT);
        if (guideY >= 0) UI.fill(ctx, 0, guideY, width, guideY + 1, UI.ACCENT);

        for (HudModule h : active()) {
            boolean hot = h == dragged || h == selected;
            UI.frame(ctx, h.x - 2, h.y - 2, h.w + 4, h.h + 4, hot ? UI.ACCENT : 0xFF8A8A96);
        }
        if (selected != null) {
            String label = selected.name + "  (" + selected.x + ", " + selected.y + ")";
            UI.text(ctx, textRenderer, label, selected.x, Math.max(2, selected.y - 13), UI.TEXT);
        }
    }

    /** Snap one axis. Returns the snapped position and records the guide line (or -1). */
    private int[] snapAxis(int pos, int size, int screen, boolean xAxis, HudModule self) {
        List<int[]> cands = new ArrayList<>();            // {position, guide}
        cands.add(new int[]{0, 0});
        cands.add(new int[]{screen - size, screen - 1});
        cands.add(new int[]{(screen - size) / 2, screen / 2});
        for (HudModule o : active()) {
            if (o == self) continue;
            int os = xAxis ? o.x : o.y, ol = xAxis ? o.w : o.h;
            cands.add(new int[]{os, os});
            cands.add(new int[]{os + ol - size, os + ol});
            cands.add(new int[]{os + ol, os + ol});
            cands.add(new int[]{os - size, os});
        }
        int best = pos, guide = -1, bestD = SNAP_DIST + 1;
        for (int[] c : cands) {
            int d = Math.abs(c[0] - pos);
            if (d < bestD) { bestD = d; best = c[0]; guide = c[1]; }
        }
        return new int[]{best, guide};
    }

    private void place(HudModule h, int nx, int ny, boolean snapIt) {
        nx = Math.max(0, Math.min(Math.max(0, width - h.w), nx));
        ny = Math.max(0, Math.min(Math.max(0, height - h.h), ny));
        guideX = -1; guideY = -1;
        if (snap && snapIt) {
            int[] sx = snapAxis(nx, h.w, width, true, h);
            int[] sy = snapAxis(ny, h.h, height, false, h);
            nx = Math.max(0, Math.min(Math.max(0, width - h.w), sx[0]));
            ny = Math.max(0, Math.min(Math.max(0, height - h.h), sy[0]));
            guideX = sx[1];
            guideY = sy[1];
        }
        h.x = nx; h.y = ny;
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button == 0) {
            List<HudModule> list = active();
            for (int i = list.size() - 1; i >= 0; i--) {
                HudModule h = list.get(i);
                if (mx >= h.x && mx <= h.x + h.w && my >= h.y && my <= h.y + h.h) {
                    dragged = h; selected = h;
                    offX = (int) mx - h.x; offY = (int) my - h.y;
                    return true;
                }
            }
            selected = null;
        }
        return false;
    }

    @Override
    protected boolean onDrag(double mx, double my, int button) {
        if (dragged != null) {
            place(dragged, (int) mx - offX, (int) my - offY, true);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRelease(double mx, double my, int button) {
        dragged = null;
        guideX = -1; guideY = -1;
        return false;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (key == GLFW.GLFW_KEY_G) { snap = !snap; return true; }
        if (selected != null) {
            switch (key) {
                case GLFW.GLFW_KEY_LEFT -> { place(selected, selected.x - 1, selected.y, false); return true; }
                case GLFW.GLFW_KEY_RIGHT -> { place(selected, selected.x + 1, selected.y, false); return true; }
                case GLFW.GLFW_KEY_UP -> { place(selected, selected.x, selected.y - 1, false); return true; }
                case GLFW.GLFW_KEY_DOWN -> { place(selected, selected.x, selected.y + 1, false); return true; }
                case GLFW.GLFW_KEY_R -> { selected.x = selected.defX; selected.y = selected.defY; return true; }
                default -> { }
            }
        }
        return false;
    }

    @Override public void close() { client.setScreen(parent); }
    @Override public void removed() { Config.save(); }
}
