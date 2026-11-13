package com.victusclient.gui;

import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.module.modules.ColorThemesModule;
import com.victusclient.module.modules.CustomGuiModule;
import com.victusclient.module.modules.CustomThemesModule;

/** Resolves Color Themes + Custom Themes + Custom GUI settings into the live UI colours. */
public final class Theme {
    public record Preset(String name, int accent, int accentDim, int bg, int surface, int hover, int border) {}

    public static final Preset[] PRESETS = {
            new Preset("Victus Purple", 0xFF8B5CF6, 0xFF43297D, 0xFF090713, 0xFF100D1B, 0xFF1B1530, 0xFF30234B),
            new Preset("Ocean",   0xFF3B82F6, 0xFF1B3A73, 0xFF080A0F, 0xFF0D1117, 0xFF151B26, 0xFF1F2937),
            new Preset("Emerald", 0xFF10B981, 0xFF0B5E43, 0xFF080B0A, 0xFF0D1210, 0xFF141C19, 0xFF1F2A26),
            new Preset("Violet",  0xFF8B5CF6, 0xFF43297D, 0xFF0A090F, 0xFF100F16, 0xFF191724, 0xFF26233A),
            new Preset("Sunset",  0xFFF97316, 0xFF7A3409, 0xFF0B0907, 0xFF120F0C, 0xFF1B1612, 0xFF2B231B),
            new Preset("Cyber",   0xFF22D3EE, 0xFF0E5F6E, 0xFF07090D, 0xFF0C1015, 0xFF131A22, 0xFF1C2733),
            new Preset("Mono",    0xFFE4E4E7, 0xFF52525B, 0xFF09090B, 0xFF0F0F11, 0xFF18181B, 0xFF27272A),
    };

    private Theme() {}

    public static String[] presetNames() {
        String[] n = new String[PRESETS.length];
        for (int i = 0; i < n.length; i++) n[i] = PRESETS[i].name();
        return n;
    }

    private static <T extends Module> T find(Class<T> cls) { return ModuleManager.of(cls); }

    /** Re-apply all theme settings. Cheap; called every menu frame and after config loads. */
    public static void apply() {
        ColorThemesModule ct = find(ColorThemesModule.class);
        CustomThemesModule cu = find(CustomThemesModule.class);
        CustomGuiModule cg = find(CustomGuiModule.class);

        Preset p = PRESETS[(ct == null || !ct.enabled) ? 0 : Math.min(ct.theme.index, PRESETS.length - 1)];
        int accent = p.accent(), dim = p.accentDim();
        int bg = p.bg(), surface = p.surface(), hover = p.hover(), border = p.border();

        if (cu != null && cu.enabled) {
            accent = 0xFF000000 | ((int) cu.red.value << 16) | ((int) cu.green.value << 8) | (int) cu.blue.value;
            int tint = cu.tint();
            int lift = (int) cu.surfaceLift.value;
            bg = shade(tint, 9 + lift / 2);
            surface = shade(tint, 15 + lift);
            hover = shade(tint, 23 + lift);
            border = shade(tint, 35 + lift);
            dim = UI.mix(bg, accent, 0.42f);
        }

        UI.ACCENT = accent;
        UI.ACCENT_DIM = dim;
        UI.BG = bg;
        UI.SURFACE = surface;
        UI.SURFACE_HOVER = hover;
        UI.BORDER = border;
        UI.radius = (cg != null && cg.enabled && cg.corners.get().equals("Square")) ? 0 : 2;
    }

    /** A dark colour of the given tint (0xRRGGBB scale hint) at the given brightness level (0-255). */
    private static int shade(int tint, int level) {
        int r = ((tint >> 16) & 0xFF), g = ((tint >> 8) & 0xFF), b = (tint & 0xFF);
        float k = level / 255f;
        int nr = Math.min(255, Math.round(r * k)), ng = Math.min(255, Math.round(g * k)), nb = Math.min(255, Math.round(b * k));
        return 0xFF000000 | (nr << 16) | (ng << 8) | nb;
    }
}
