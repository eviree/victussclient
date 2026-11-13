package com.victusclient.module.modules;

import com.victusclient.gui.Theme;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;

/** Preset colour themes for the menu and any HUD element set to the "Theme" colour. */
public class ColorThemesModule extends Module {
    public final Setting.Mode theme = add(new Setting.Mode("Theme", 0, Theme.presetNames()));

    public ColorThemesModule() { super("Color Themes", Category.VISUAL); }
}
