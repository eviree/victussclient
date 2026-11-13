package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;

/** Layout options for the Victus menu itself (size, columns, corners, backdrop). */
public class CustomGuiModule extends Module {
    public final Setting.Slider width = add(new Setting.Slider("Menu Width", 440, 320, 640, 10));
    public final Setting.Slider height = add(new Setting.Slider("Menu Height", 290, 200, 400, 10));
    public final Setting.Mode columns = add(new Setting.Mode("Card Columns", 0, "2", "3"));
    public final Setting.Mode corners = add(new Setting.Mode("Corners", 0, "Rounded", "Square"));
    public final Setting.Slider dim = add(new Setting.Slider("Backdrop Dim", 75, 0, 95, 5));

    public CustomGuiModule() { super("Custom GUI", Category.VISUAL); }

    public int menuWidth() { return enabled ? (int) width.value : 440; }
    public int menuHeight() { return enabled ? (int) height.value : 290; }
    public int columnCount() { return enabled && columns.index == 1 ? 3 : 2; }
    public int backdropAlpha() { return (int) ((enabled ? dim.value : 75) * 2.55); }
}
