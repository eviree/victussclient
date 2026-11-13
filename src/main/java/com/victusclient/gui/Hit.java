package com.victusclient.gui;

import com.victusclient.setting.Setting;

/** Clickable rectangle registered during render; checked on mouse input. */
final class Hit {
    int x, y, w, h;
    Runnable left, right;
    Setting.Slider slider;
    boolean scroll;

    Hit(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }

    boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
}
