package com.victusclient.gui;

import com.victusclient.compat.VScreen;
import com.victusclient.config.Config;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.module.modules.BrandingModule;
import com.victusclient.module.modules.CustomGuiModule;
import com.victusclient.module.modules.MenuAnimationsModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Victus Mod Menu: card grid, filter pills, favorites, animated switches, settings pages, keybind editor. */
public class ClickGUIScreen extends VScreen {
    private final List<Hit> hits = new ArrayList<>();
    private final Map<String, Float> anims = new HashMap<>();
    private Hit dragging;
    private String search = "";
    private String filter = "All";
    private Module selected;
    private Setting.Key listeningKey;
    private Setting.Text editingText;
    private double scroll, scrollTarget;
    private int maxScroll, viewTop, viewBottom;
    private final long openNs = System.nanoTime();
    private long lastNs = System.nanoTime();
    private float dt;
    private boolean animOn;
    private MenuAnimationsModule ma;

    public ClickGUIScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldPause() { return false; }

    // ---- helpers ----------------------------------------------------------------------------

    private boolean matches(Module m) {
        if (!search.isEmpty() && !m.name.toLowerCase().contains(search.toLowerCase())) return false;
        if (filter.equals("All")) return true;
        if (filter.equals("Favorites")) return m.favorite;
        return m.category.label.equals(filter);
    }

    private void resetScroll() { scroll = 0; scrollTarget = 0; }

    private float approach(String key, float target, boolean enabled) {
        if (!enabled) { anims.put(key, target); return target; }
        float cur = anims.getOrDefault(key, target);
        cur += (target - cur) * Math.min(1f, dt * 14f);
        if (Math.abs(target - cur) < 0.01f) cur = target;
        anims.put(key, cur);
        return cur;
    }

    private void toggleSwitch(DrawContext ctx, String key, int x, int y, boolean on) {
        float t = approach(key, on ? 1f : 0f, animOn && ma.switches.value);
        UI.rrect(ctx, x, y, 26, 12, UI.mix(0xFF2A2A33, UI.ACCENT, t));
        UI.rrect(ctx, x + 2 + Math.round(13 * t), y + 2, 9, 8, UI.mix(0xFF6B6B76, 0xFFFFFFFF, t));
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1f, u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    private static String keyName(int key) {
        if ((key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) || (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9))
            return String.valueOf((char) key);
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) return "Num " + (key - GLFW.GLFW_KEY_KP_0);
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-Alt";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_PAGE_UP -> "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PgDn";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            default -> "Key " + key;
        };
    }

    private void drawButton(DrawContext ctx, Hit b, String label, int mx, int my, boolean primary) {
        boolean hov = b.in(mx, my);
        if (primary) {
            UI.rrect(ctx, b.x, b.y, b.w, b.h, hov ? 0xFFE4E4E7 : 0xFFFFFFFF);
            UI.centered(ctx, textRenderer, label, b.x + b.w / 2, b.y + (b.h - 8) / 2, 0xFF000000);
        } else {
            UI.card(ctx, b.x, b.y, b.w, b.h, hov ? UI.SURFACE_HOVER : UI.SURFACE, UI.BORDER);
            UI.centered(ctx, textRenderer, label, b.x + b.w / 2, b.y + (b.h - 8) / 2, UI.TEXT);
        }
    }

    // ---- render -----------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        dt = Math.min(0.1f, (now - lastNs) / 1e9f);
        lastNs = now;

        Theme.apply();
        ma = ModuleManager.of(MenuAnimationsModule.class);
        animOn = ma != null && ma.enabled;

        int yOff = 0;
        UI.alpha = 1f;
        if (animOn && ma.open.index != 0) {
            float raw = Math.min(1f, (now - openNs) / 1e6f / (float) ma.duration.value);
            float e = 1f - (1f - raw) * (1f - raw) * (1f - raw);
            switch (ma.open.get()) {
                case "Fade" -> UI.alpha = e;
                case "Slide Up" -> { UI.alpha = e; yOff = Math.round((1f - e) * 26); }
                case "Slide Down" -> { UI.alpha = e; yOff = -Math.round((1f - e) * 26); }
                case "Pop" -> { UI.alpha = Math.min(1f, raw * 1.8f); yOff = Math.round((1f - easeOutBack(raw)) * 20); }
                default -> { }
            }
        }
        try {
            renderMenu(ctx, mx, my, yOff);
        } finally {
            UI.alpha = 1f;
        }
    }

    private void renderMenu(DrawContext ctx, int mx, int my, int yOff) {
        hits.clear();
        CustomGuiModule cg = ModuleManager.of(CustomGuiModule.class);
        BrandingModule br = ModuleManager.of(BrandingModule.class);

        UI.fill(ctx, 0, 0, width, height, (cg.backdropAlpha() & 0xFF) << 24);

        int pw = Math.min(width - 20, cg.menuWidth()), ph = Math.min(height - 20, cg.menuHeight());
        int px = (width - pw) / 2, py = (height - ph) / 2 + yOff;
        int headerH = 42, footerH = 28;

        UI.card(ctx, px, py, pw, ph, UI.BG, UI.BORDER);

        // Header: logo + wordmark + tagline (Logo & Branding settings)
        int hx = px + 14;
        String motion = br.motion.get();
        float speed = br.motionSpeed();
        if (br.showIcon()) { Logo.icon(ctx, hx, py + 9, 24, motion, speed); hx += 31; }
        if (br.showText()) Logo.title(ctx, textRenderer, br.name(), hx, py + 11, motion, speed);
        String sub = selected == null ? br.tagline.value : selected.name.toUpperCase();
        UI.text(ctx, textRenderer, sub, hx, py + 24, UI.MUTED);
        UI.fill(ctx, px + 1, py + headerH, px + pw - 1, py + headerH + 1, UI.BORDER);

        int cx1 = px + 10, cx2 = px + pw - 10;
        int top = py + headerH + 9;

        if (selected == null) {
            // filter pills (wrap onto more rows when the menu is narrow)
            List<String> filters = new ArrayList<>(List.of("All"));
            for (Category c : Category.values()) filters.add(c.label);
            filters.add("Favorites");
            int fx = cx1, fy = py + headerH + 7;
            for (String f : filters) {
                int fw = textRenderer.getWidth(f) + 14;
                if (fx + fw > cx2 && fx > cx1) { fx = cx1; fy += 19; }
                Hit pill = new Hit(fx, fy, fw, 15);
                pill.left = () -> { filter = f; resetScroll(); };
                hits.add(pill);
                boolean sel = f.equals(filter);
                float hv = approach("pill:" + f, pill.in(mx, my) ? 1f : 0f, animOn && ma.hover.value);
                UI.card(ctx, pill.x, pill.y, fw, 15, sel ? UI.ACCENT : UI.mix(UI.SURFACE, UI.SURFACE_HOVER, hv),
                        sel ? UI.ACCENT : UI.BORDER);
                UI.text(ctx, textRenderer, f, pill.x + 7, pill.y + 4, sel ? UI.TEXT : UI.MUTED);
                fx += fw + 5;
            }
            top = fy + 15 + 7;
        } else {
            Hit back = new Hit(px + pw - 66, py + 14, 54, 15);
            back.left = () -> { selected = null; listeningKey = null; editingText = null; resetScroll(); };
            hits.add(back);
            drawButton(ctx, back, "< Back", mx, my, false);
        }

        viewTop = top;
        viewBottom = py + ph - footerH - 4;
        ctx.enableScissor(px, viewTop, px + pw, viewBottom);
        int contentH = selected == null ? drawGrid(ctx, mx, my, cx1, cx2, cg) : drawSettings(ctx, mx, my, cx1, cx2);
        ctx.disableScissor();
        maxScroll = Math.max(0, contentH - (viewBottom - viewTop));
        scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
        if (animOn && ma.smoothScroll.value) {
            scroll += (scrollTarget - scroll) * Math.min(1f, dt * 16f);
            if (Math.abs(scrollTarget - scroll) < 0.5) scroll = scrollTarget;
        } else {
            scroll = scrollTarget;
        }
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        // Footer: search + Profiles + HUD Editor
        int fy = py + ph - footerH;
        UI.fill(ctx, px + 1, fy, px + pw - 1, fy + 1, UI.BORDER);
        String s = search.isEmpty() ? "Search mods..." : search + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        UI.text(ctx, textRenderer, s, px + 14, fy + 10, search.isEmpty() ? UI.MUTED : UI.TEXT);

        Hit btn = new Hit(px + pw - 108, fy + 5, 96, 19);
        btn.left = () -> client.setScreen(new HudEditorScreen(this));
        hits.add(btn);
        drawButton(ctx, btn, "HUD EDITOR", mx, my, true);

        Hit prof = new Hit(px + pw - 108 - 6 - 74, fy + 5, 74, 19);
        prof.left = () -> client.setScreen(new ProfilesScreen(this));
        hits.add(prof);
        drawButton(ctx, prof, "PROFILES", mx, my, false);
    }

    private int drawGrid(DrawContext ctx, int mx, int my, int x1, int x2, CustomGuiModule cg) {
        int cols = cg.columnCount();
        int gap = 8, cw = (x2 - x1 - gap * (cols - 1)) / cols, ch = 42;
        int i = 0;
        for (Module m : ModuleManager.all()) {
            if (!matches(m)) continue;
            int cx = x1 + (i % cols) * (cw + gap);
            int cy = viewTop + (i / cols) * (ch + gap) - (int) scroll;
            i++;
            if (cy + ch < viewTop || cy > viewBottom) continue;

            Hit heart = new Hit(cx + cw - 22, cy + 3, 18, 14);
            heart.left = () -> m.favorite = !m.favorite;
            Hit sw = new Hit(cx + cw - 38, cy + ch - 22, 30, 18);
            sw.left = m::toggle;
            Hit card = new Hit(cx, cy, cw, ch);
            // modules with only the Toggle Key setting toggle on click; right-click always opens settings
            card.left = m.settings.size() <= 1 ? m::toggle : () -> { selected = m; resetScroll(); };
            card.right = () -> { selected = m; resetScroll(); };
            for (Hit h : new Hit[]{heart, sw, card}) { h.scroll = true; hits.add(h); }

            boolean hover = card.in(mx, my) && my >= viewTop && my <= viewBottom;
            float hv = approach(m.name + "#h", hover ? 1f : 0f, animOn && ma.hover.value);
            float en = approach(m.name + "#e", m.enabled ? 1f : 0f, animOn && ma.hover.value);
            UI.card(ctx, cx, cy, cw, ch, UI.mix(UI.SURFACE, UI.SURFACE_HOVER, hv), UI.mix(UI.BORDER, UI.ACCENT_DIM, en));

            // icon tile
            UI.card(ctx, cx + 7, cy + 7, 28, 28, 0xFF0A0A0D, m.enabled ? UI.ACCENT_DIM : UI.BORDER);
            String ini = m.name.substring(0, 1);
            UI.text(ctx, textRenderer, ini, cx + 21 - textRenderer.getWidth(ini) / 2, cy + 17, m.enabled ? UI.ACCENT : UI.MUTED);

            int textW = cw - 43 - 22;
            UI.text(ctx, textRenderer, textRenderer.trimToWidth(m.name, textW), cx + 43, cy + 12, m.enabled ? UI.TEXT : 0xFFB4B4BE);
            String status = m.enabled ? "ACTIVE" : "OFF";
            if (m.bind.isBound()) status += "  [" + keyName(m.bind.key) + "]";
            UI.text(ctx, textRenderer, textRenderer.trimToWidth(status, cw - 43 - 40), cx + 43, cy + 26,
                    m.enabled ? UI.GREEN : UI.MUTED);
            UI.text(ctx, textRenderer, "\u2665", cx + cw - 15, cy + 6, m.favorite ? UI.ACCENT : 0xFF4A4A55);
            toggleSwitch(ctx, m.name + "#s", cx + cw - 36, cy + ch - 19, m.enabled);
        }
        int rows = (i + cols - 1) / cols;
        return rows == 0 ? 0 : rows * (ch + gap) - gap;
    }

    private int drawSettings(DrawContext ctx, int mx, int my, int x1, int x2) {
        int w = x2 - x1, rh = 24, y = viewTop - (int) scroll, total = 0;
        Module m = selected;

        Hit en = new Hit(x1, y, w, rh);
        en.left = m::toggle; en.scroll = true; hits.add(en);
        UI.card(ctx, x1, y, w, rh, en.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE, UI.BORDER);
        UI.text(ctx, textRenderer, "Enabled", x1 + 10, y + 8, UI.TEXT);
        toggleSwitch(ctx, m.name + "#es", x2 - 36, y + 6, m.enabled);
        y += rh + 5; total += rh + 5;

        for (Setting st : m.settings) {
            Hit h = new Hit(x1, y, w, rh);
            h.scroll = true;
            UI.card(ctx, x1, y, w, rh, UI.SURFACE, UI.BORDER);
            if (st instanceof Setting.Bool b) {
                h.left = b::toggle;
                UI.text(ctx, textRenderer, b.name, x1 + 10, y + 8, 0xFFD4D4DC);
                toggleSwitch(ctx, m.name + "/" + b.name, x2 - 36, y + 6, b.value);
            } else if (st instanceof Setting.Slider sl) {
                h.slider = sl;
                int fillW = (int) ((w - 2) * sl.fraction());
                if (fillW > 0) UI.rrect(ctx, x1 + 1, y + 1, Math.max(fillW, 4), rh - 2, UI.ACCENT_DIM);
                UI.text(ctx, textRenderer, sl.name, x1 + 10, y + 8, UI.TEXT);
                String v = sl.display();
                UI.text(ctx, textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, UI.TEXT);
            } else if (st instanceof Setting.Mode md) {
                h.left = md::next; h.right = md::prev;
                UI.text(ctx, textRenderer, md.name, x1 + 10, y + 8, 0xFFD4D4DC);
                String v = "< " + md.get() + " >";
                UI.text(ctx, textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, UI.ACCENT);
            } else if (st instanceof Setting.Key k) {
                boolean listening = listeningKey == k;
                h.left = () -> { editingText = null; listeningKey = listening ? null : k; };
                h.right = () -> { k.key = -1; listeningKey = null; };
                UI.text(ctx, textRenderer, k.name, x1 + 10, y + 8, 0xFFD4D4DC);
                String v = listening ? "Press a key...  (Esc cancel, Del clear)"
                        : (k.isBound() ? "[ " + keyName(k.key) + " ]" : "[ None ]");
                UI.text(ctx, textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, listening ? UI.TEXT : UI.ACCENT);
            } else if (st instanceof Setting.Text t) {
                boolean editing = editingText == t;
                h.left = () -> { listeningKey = null; editingText = t; };
                UI.text(ctx, textRenderer, t.name, x1 + 10, y + 8, 0xFFD4D4DC);
                String v = t.value + (editing && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
                UI.text(ctx, textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, editing ? UI.TEXT : UI.ACCENT);
            }
            hits.add(h);
            y += rh + 5; total += rh + 5;
        }
        return total;
    }

    private void updateSlider(Hit h, double mx) { h.slider.setFraction((mx - h.x) / h.w); }

    // ---- input (version-neutral callbacks from VScreen) -------------------------------------

    @Override
    protected boolean onClick(double mx, double my, int button) {
        Setting.Key lk = listeningKey;
        Setting.Text et = editingText;
        boolean consumed = false;
        for (Hit h : hits) {
            if (!h.in(mx, my)) continue;
            if (h.scroll && (my < viewTop || my > viewBottom)) continue;
            if (button == 0 && h.slider != null) { dragging = h; updateSlider(h, mx); }
            else if (button == 0 && h.left != null) h.left.run();
            else if (button == 1 && h.right != null) h.right.run();
            consumed = true;
            break;
        }
        // clicking anywhere else ends key capture / text editing
        if (listeningKey == lk) listeningKey = null;
        if (editingText == et) editingText = null;
        return consumed;
    }

    @Override
    protected boolean onDrag(double mx, double my, int button) {
        if (dragging != null) { updateSlider(dragging, mx); return true; }
        return false;
    }

    @Override
    protected boolean onRelease(double mx, double my, int button) {
        dragging = null;
        return false;
    }

    @Override
    protected boolean onScroll(double mx, double my, double vertical) {
        scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget - vertical * 20));
        return true;
    }

    @Override
    protected boolean onKey(int key) {
        if (listeningKey != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { /* cancel */ }
            else if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) listeningKey.key = -1;
            else listeningKey.key = key;
            listeningKey = null;
            return true;
        }
        if (editingText != null) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
                editingText = null;
            } else if (key == GLFW.GLFW_KEY_BACKSPACE && !editingText.value.isEmpty()) {
                editingText.value = editingText.value.substring(0, editingText.value.length() - 1);
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (key == GLFW.GLFW_KEY_ESCAPE && selected != null) { selected = null; resetScroll(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onChar(int cp) {
        if (listeningKey != null) return true;
        if (editingText != null) {
            if (cp >= 32 && cp < 127 && editingText.value.length() < editingText.max)
                editingText.value += (char) cp;
            return true;
        }
        if (cp >= 32 && cp != 127 && search.length() < 20) {
            search += new String(Character.toChars(cp));
            selected = null; resetScroll();
            return true;
        }
        return false;
    }

    @Override public void removed() { UI.alpha = 1f; Config.save(); }
}
