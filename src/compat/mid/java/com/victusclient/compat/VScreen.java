package com.victusclient.compat;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Version-neutral screen base: subclasses override onClick/onKey/... instead of version-specific methods. */
public abstract class VScreen extends Screen {
    protected VScreen(Text title) { super(title); }


    /** Minecraft 1.21.6+ renders the panorama/blur backdrop before render(), so there is nothing to do here. */
    protected void drawBackdrop(net.minecraft.client.gui.DrawContext ctx, int mx, int my, float delta) { }

    protected boolean onClick(double x, double y, int button) { return false; }
    protected boolean onDrag(double x, double y, int button) { return false; }
    protected boolean onRelease(double x, double y, int button) { return false; }
    protected boolean onKey(int key) { return false; }
    protected boolean onChar(int codepoint) { return false; }
    protected boolean onScroll(double x, double y, double amount) { return false; }

    @Override public boolean mouseClicked(double x, double y, int b) { return onClick(x, y, b) || super.mouseClicked(x, y, b); }
    @Override public boolean mouseDragged(double x, double y, int b, double dx, double dy) { return onDrag(x, y, b) || super.mouseDragged(x, y, b, dx, dy); }
    @Override public boolean mouseReleased(double x, double y, int b) { return onRelease(x, y, b) || super.mouseReleased(x, y, b); }
    @Override public boolean keyPressed(int key, int scan, int mods) { return onKey(key) || super.keyPressed(key, scan, mods); }
    @Override public boolean charTyped(char c, int mods) { return onChar(c) || super.charTyped(c, mods); }
    @Override public boolean mouseScrolled(double x, double y, double h, double v) { return onScroll(x, y, v) || super.mouseScrolled(x, y, h, v); }
}
