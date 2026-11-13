package com.victusclient.module;

public enum Category {
    HUD("HUD"), PVP("PvP"), VISUAL("Visual"), PERFORMANCE("Performance"), MOVEMENT("Movement"), CUSTOMIZE("Customize");
    public final String label;
    Category(String label) { this.label = label; }
}
