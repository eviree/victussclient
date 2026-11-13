package com.victusclient.compat;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** Version-neutral screen base for Minecraft 1.21.9+ (event-object input API). */
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

    @Override public boolean mouseClicked(Click c, boolean doubled) { return onClick(c.x(), c.y(), c.button()) || super.mouseClicked(c, doubled); }
    @Override public boolean mouseDragged(Click c, double dx, double dy) { return onDrag(c.x(), c.y(), c.button()) || super.mouseDragged(c, dx, dy); }
    @Override public boolean mouseReleased(Click c) { return onRelease(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
    @Override public boolean keyPressed(KeyInput in) { return onKey(in.getKeycode()) || super.keyPressed(in); }
    @Override public boolean charTyped(CharInput in) { return (in.isValidChar() && onChar(in.codepoint())) || super.charTyped(in); }
    @Override public boolean mouseScrolled(double x, double y, double h, double v) { return onScroll(x, y, v) || super.mouseScrolled(x, y, h, v); }
}
