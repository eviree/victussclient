package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;

/** Build your own theme: accent colour + background tint. Overrides Color Themes while enabled. */
public class CustomThemesModule extends Module {
    public final Setting.Slider red = add(new Setting.Slider("Accent Red", 225, 0, 255, 1));
    public final Setting.Slider green = add(new Setting.Slider("Accent Green", 29, 0, 255, 1));
    public final Setting.Slider blue = add(new Setting.Slider("Accent Blue", 72, 0, 255, 1));
    public final Setting.Mode tintMode = add(new Setting.Mode("Background Tint", 0,
            "Neutral", "Cool", "Warm", "Green", "Purple"));
    public final Setting.Slider surfaceLift = add(new Setting.Slider("Surface Brightness", 0, 0, 20, 1));

    public CustomThemesModule() { super("Custom Themes", Category.CUSTOMIZE); }

    public int tint() {
        return switch (tintMode.index) {
            case 1 -> 0xFFC8DCFF;
            case 2 -> 0xFFFFE6C8;
            case 3 -> 0xFFC8FFE0;
            case 4 -> 0xFFE6C8FF;
            default -> 0xFFFFFFFF;
        };
    }
}
