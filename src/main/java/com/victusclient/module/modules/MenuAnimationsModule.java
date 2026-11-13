package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;

/** Open animation, animated switches, hover glow and smooth scrolling for the Victus menu. */
public class MenuAnimationsModule extends Module {
    public final Setting.Mode open = add(new Setting.Mode("Open Animation", 3, "None", "Fade", "Slide Up", "Slide Down", "Pop"));
    public final Setting.Slider duration = add(new Setting.Slider("Duration (ms)", 240, 80, 600, 20));
    public final Setting.Bool switches = add(new Setting.Bool("Animated Switches", true));
    public final Setting.Bool hover = add(new Setting.Bool("Hover Glow", true));
    public final Setting.Bool smoothScroll = add(new Setting.Bool("Smooth Scroll", true));

    public MenuAnimationsModule() { super("Menu Animations", Category.CUSTOMIZE); }
}
