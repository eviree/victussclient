package com.victusclient.module.modules;

import com.victusclient.gui.Logo;
import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Logo & branding. The menu header always uses these settings; enabling the module also shows the
 * logo as a draggable HUD watermark.
 */
public class BrandingModule extends HudModule {
    public final Setting.Text brandName = add(new Setting.Text("Brand Name", "VICTUS CLIENT", 18));
    public final Setting.Text tagline = add(new Setting.Text("Tagline", "MOD MENU", 18));
    public final Setting.Mode logoStyle = add(new Setting.Mode("Logo Style", 0, "Icon + Text", "Text Only", "Icon Only"));
    public final Setting.Mode motion = add(new Setting.Mode("Logo Motion", 0, "Pulse", "Shimmer", "Wave", "Rainbow", "None"));
    public final Setting.Slider speed = add(new Setting.Slider("Motion Speed", 5, 1, 10, 1));

    public BrandingModule() { super("Logo & Branding", Category.CUSTOMIZE, 200, 6); w = 90; h = 20; }

    public boolean showIcon() { return logoStyle.index != 1; }
    public boolean showText() { return logoStyle.index != 2; }
    public String name() { return brandName.value.isBlank() ? "VICTUS CLIENT" : brandName.value; }
    public float motionSpeed() { return (float) speed.value / 5f; }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        int icon = 14;
        int tx = x + 5;
        int textW = showText() ? Logo.titleWidth(mc.textRenderer, name()) : 0;
        w = 10 + (showIcon() ? icon + (showText() ? 5 : 0) : 0) + textW;
        h = 20;
        if (background.value) ctx.fill(x, y, x + w, y + h, bgColor());
        if (showIcon()) {
            Logo.icon(ctx, x + 5, y + 3, icon, motion.get(), motionSpeed());
            tx += icon + 5;
        }
        if (showText()) Logo.title(ctx, mc.textRenderer, name(), tx, y + 6, motion.get(), motionSpeed());
    }
}
