package com.victusclient.module;

import net.minecraft.client.option.SimpleOption;

/**
 * Enum-option helpers that never name the enum type. Some option enums (e.g. ParticlesMode) moved to a
 * different package in newer Minecraft versions, so naming them would break one of the builds.
 */
public final class OptionUtil {
    private OptionUtil() {}

    /** Sets an enum option to the constant at the given ordinal (clamped). */
    public static <E extends Enum<E>> void setByOrdinal(SimpleOption<E> opt, int ordinal) {
        E cur = opt.getValue();
        E[] all = cur.getDeclaringClass().getEnumConstants();
        E want = all[Math.max(0, Math.min(ordinal, all.length - 1))];
        if (cur != want) opt.setValue(want);
    }

    /** Puts back a value previously read from the same option. */
    @SuppressWarnings("unchecked")
    public static <T> void restore(SimpleOption<T> opt, Object value) {
        opt.setValue((T) value);
    }
}
