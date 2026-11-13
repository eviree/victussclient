package com.victusclient.gui;

import com.victusclient.compat.VScreen;
import com.victusclient.config.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Mod Profiles: switch between built-in layouts and your own saved setups. */
public class ProfilesScreen extends VScreen {
    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private String input = "";
    private String status = "";
    private int statusColor = UI.GREEN;
    private long statusUntil;
    private String confirmDelete = "";
    private long confirmUntil;
    private double scroll;
    private int maxScroll, viewTop, viewBottom;

    public ProfilesScreen(Screen parent) {
        super(Text.literal("Profiles"));
        this.parent = parent;
    }

    @Override public boolean shouldPause() { return false; }

    private void say(String s, boolean ok) {
        status = s;
        statusColor = ok ? UI.GREEN : UI.ACCENT;
        statusUntil = System.currentTimeMillis() + 3500;
    }

    private void button(DrawContext ctx, Hit b, String label, int mx, int my, boolean primary, int textCol) {
        boolean hov = b.in(mx, my);
        if (primary) {
            UI.rrect(ctx, b.x, b.y, b.w, b.h, hov ? 0xFFE4E4E7 : 0xFFFFFFFF);
            UI.centered(ctx, textRenderer, label, b.x + b.w / 2, b.y + (b.h - 8) / 2, 0xFF000000);
        } else {
            UI.card(ctx, b.x, b.y, b.w, b.h, hov ? UI.SURFACE_HOVER : UI.BG, UI.BORDER);
            UI.centered(ctx, textRenderer, label, b.x + b.w / 2, b.y + (b.h - 8) / 2, textCol);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        hits.clear();
        UI.fill(ctx, 0, 0, width, height, 0xC0000000);

        int pw = Math.min(width - 20, 380), ph = Math.min(height - 20, 270);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        int headerH = 42, footerH = 32;
        UI.card(ctx, px, py, pw, ph, UI.BG, UI.BORDER);

        UI.text(ctx, textRenderer, Text.literal("PROFILES").formatted(Formatting.BOLD), px + 14, py + 10, UI.TEXT);
        boolean showStatus = System.currentTimeMillis() < statusUntil;
        String sub = showStatus ? status : (Config.activeProfile.isEmpty() ? "No profile loaded" : "Active: " + Config.activeProfile);
        UI.text(ctx, textRenderer, sub, px + 14, py + 24, showStatus ? statusColor : UI.MUTED);
        UI.fill(ctx, px + 1, py + headerH, px + pw - 1, py + headerH + 1, UI.BORDER);

        Hit back = new Hit(px + pw - 66, py + 14, 54, 15);
        back.left = this::close;
        hits.add(back);
        button(ctx, back, "< Back", mx, my, false, UI.TEXT);

        // list
        int x1 = px + 10, x2 = px + pw - 10, rh = 24, gap = 5;
        viewTop = py + headerH + 9;
        viewBottom = py + ph - footerH - 4;
        List<String> saved = Config.profiles();
        List<String> all = new ArrayList<>(Config.BUILTIN);
        all.addAll(saved);

        ctx.enableScissor(px, viewTop, px + pw, viewBottom);
        int y = viewTop - (int) scroll;
        for (String name : all) {
            boolean builtin = Config.isBuiltin(name);
            boolean active = name.equalsIgnoreCase(Config.activeProfile);
            if (y + rh >= viewTop && y <= viewBottom) {
                UI.card(ctx, x1, y, x2 - x1, rh, UI.SURFACE, active ? UI.ACCENT_DIM : UI.BORDER);
                String shown = textRenderer.trimToWidth(name, 120);
                UI.text(ctx, textRenderer, shown, x1 + 10, y + 8, active ? UI.TEXT : 0xFFD4D4DC);
                if (builtin) UI.text(ctx, textRenderer, "BUILT-IN", x1 + 14 + textRenderer.getWidth(shown), y + 8, UI.MUTED);

                int bx = x2 - 6;
                if (!builtin) {
                    bx -= 34;
                    Hit del = new Hit(bx, y + 4, 34, rh - 8);
                    boolean sure = confirmDelete.equals(name) && System.currentTimeMillis() < confirmUntil;
                    del.left = () -> {
                        if (confirmDelete.equals(name) && System.currentTimeMillis() < confirmUntil) {
                            confirmDelete = "";
                            say(Config.deleteProfile(name) ? "Deleted " + name : "Could not delete " + name, true);
                        } else { confirmDelete = name; confirmUntil = System.currentTimeMillis() + 2500; }
                    };
                    del.scroll = true; hits.add(del);
                    button(ctx, del, sure ? "Sure?" : "Del", mx, my, false, UI.ACCENT);
                    bx -= 38;
                    Hit sv = new Hit(bx, y + 4, 34, rh - 8);
                    sv.left = () -> say(Config.saveProfile(name) ? "Saved " + name : "Could not save " + name, true);
                    sv.scroll = true; hits.add(sv);
                    button(ctx, sv, "Save", mx, my, false, UI.TEXT);
                    bx -= 4;
                }
                bx -= 38;
                Hit ld = new Hit(bx, y + 4, 38, rh - 8);
                ld.left = () -> say(Config.loadProfile(name) ? "Loaded " + name : "Could not load " + name, true);
                ld.scroll = true; hits.add(ld);
                button(ctx, ld, "Load", mx, my, true, UI.TEXT);
            }
            y += rh + gap;
        }
        ctx.disableScissor();
        int contentH = Math.max(0, all.size() * (rh + gap) - gap);
        maxScroll = Math.max(0, contentH - (viewBottom - viewTop));
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        // footer: name field + Save As
        int fy = py + ph - footerH;
        UI.fill(ctx, px + 1, fy, px + pw - 1, fy + 1, UI.BORDER);
        int fw = pw - 28 - 92;
        UI.card(ctx, px + 10, fy + 7, fw, 18, UI.SURFACE, UI.BORDER);
        String shown = input.isEmpty() ? "New profile name..."
                : input + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        UI.text(ctx, textRenderer, shown, px + 17, fy + 12, input.isEmpty() ? UI.MUTED : UI.TEXT);

        Hit sa = new Hit(px + pw - 100, fy + 6, 90, 20);
        sa.left = this::saveAs;
        hits.add(sa);
        button(ctx, sa, "SAVE AS", mx, my, true, UI.TEXT);
    }

    private void saveAs() {
        String name = Config.sanitize(input);
        if (name.isEmpty()) name = "Profile " + (Config.profiles().size() + 1);
        if (Config.isBuiltin(name)) { say("That name is reserved", false); return; }
        if (Config.saveProfile(name)) { say("Saved " + name, true); input = ""; }
        else say("Could not save profile", false);
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button != 0) return false;
        for (Hit h : hits) {
            if (!h.in(mx, my)) continue;
            if (h.scroll && (my < viewTop || my > viewBottom)) continue;
            if (h.left != null) { h.left.run(); return true; }
        }
        return false;
    }

    @Override
    protected boolean onScroll(double mx, double my, double vertical) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - vertical * 20));
        return true;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { saveAs(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onChar(int cp) {
        if (cp < 128 && (Character.isLetterOrDigit(cp) || cp == ' ' || cp == '-' || cp == '_') && input.length() < 20) {
            input += (char) cp;
            return true;
        }
        return false;
    }

    @Override public void close() { client.setScreen(parent); }
    @Override public void removed() { Config.save(); }
}
