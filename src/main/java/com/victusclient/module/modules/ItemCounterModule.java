package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class ItemCounterModule extends HudModule {
    private final Setting.Bool pearls = add(new Setting.Bool("Ender Pearls", true));
    private final Setting.Bool totems = add(new Setting.Bool("Totems", true));
    private final Setting.Bool gapples = add(new Setting.Bool("Golden Apples", true));
    private final Setting.Bool potions = add(new Setting.Bool("Splash Potions", true));
    private final Setting.Bool arrows = add(new Setting.Bool("Arrows", false));

    public ItemCounterModule() { super("Item Counter", Category.PVP, 6, 290); }

    private int count(PlayerInventory inv, Item... items) {
        int n = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            for (Item it : items) if (s.isOf(it)) n += s.getCount();
        }
        return n;
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();
        List<String> ls = new ArrayList<>();
        if (pearls.value) ls.add("Pearls: " + count(inv, Items.ENDER_PEARL));
        if (totems.value) ls.add("Totems: " + count(inv, Items.TOTEM_OF_UNDYING));
        if (gapples.value) ls.add("Gapples: " + count(inv, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        if (potions.value) ls.add("Potions: " + count(inv, Items.SPLASH_POTION));
        if (arrows.value) ls.add("Arrows: " + count(inv, Items.ARROW));
        lines(ctx, mc, ls);
    }
}
