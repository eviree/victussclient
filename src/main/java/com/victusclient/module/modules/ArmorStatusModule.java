package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ArmorStatusModule extends HudModule {
    private final Setting.Bool showDur = add(new Setting.Bool("Show Durability", true));
    private final Setting.Bool percent = add(new Setting.Bool("Use Percent", true));
    private final Setting.Bool held = add(new Setting.Bool("Include Held Item", false));

    public ArmorStatusModule() { super("Armor Status", Category.PVP, 6, 200); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        List<ItemStack> items = new ArrayList<>();
        items.add(mc.player.getEquippedStack(EquipmentSlot.HEAD));
        items.add(mc.player.getEquippedStack(EquipmentSlot.CHEST));
        items.add(mc.player.getEquippedStack(EquipmentSlot.LEGS));
        items.add(mc.player.getEquippedStack(EquipmentSlot.FEET));
        if (held.value) items.add(mc.player.getMainHandStack());

        int rowH = 18;
        w = 80;
        h = items.size() * rowH + 4;
        if (background.value) ctx.fill(x, y, x + w, y + h, bgColor());
        int yy = y + 2;
        for (ItemStack s : items) {
            if (!s.isEmpty()) {
                ctx.drawItem(s, x + 3, yy);
                if (s.isDamageable() && showDur.value && s.getMaxDamage() > 0) {
                    int left = s.getMaxDamage() - s.getDamage();
                    float frac = (float) left / s.getMaxDamage();
                    String t = percent.value ? (int) (frac * 100) + "%" : String.valueOf(left);
                    int col = frac > 0.5f ? 0xFF55FF55 : frac > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
                    ctx.drawText(mc.textRenderer, t, x + 24, yy + 5, col, shadow.value);
                } else if (s.getCount() > 1) {
                    ctx.drawText(mc.textRenderer, "x" + s.getCount(), x + 24, yy + 5, textColor(), shadow.value);
                }
            }
            yy += rowH;
        }
    }
}
