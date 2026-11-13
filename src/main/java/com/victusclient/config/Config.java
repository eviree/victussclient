package com.victusclient.config;

import com.google.gson.*;
import com.victusclient.gui.Theme;
import com.victusclient.module.HudModule;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.module.modules.PerformancePresetsModule;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String META = "_meta";

    /** True while a config/profile is being applied (modules use it to avoid re-applying game options). */
    public static boolean loading;
    /** Name of the last loaded/saved profile (shown in the Profiles screen). */
    public static String activeProfile = "";

    /** Built-in profiles: they switch modules on/off, they never touch your saved profiles. */
    public static final List<String> BUILTIN = List.of("PvP", "Survival", "Minimal", "Performance");

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("victusclient.json"); }
    private static Path profileDir() { return FabricLoader.getInstance().getConfigDir().resolve("victusclient-profiles"); }

    // ---- snapshot / apply -------------------------------------------------------------------

    private static JsonObject snapshot() {
        JsonObject root = new JsonObject();
        for (Module m : ModuleManager.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            o.addProperty("favorite", m.favorite);
            if (m instanceof HudModule h) { o.addProperty("x", h.x); o.addProperty("y", h.y); }
            JsonObject s = new JsonObject();
            for (Setting st : m.settings) s.add(st.name, st.save());
            o.add("settings", s);
            root.add(m.name, o);
        }
        return root;
    }

    private static void apply(JsonObject root) {
        loading = true;
        try {
            for (Module m : ModuleManager.all()) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("favorite")) m.favorite = o.get("favorite").getAsBoolean();
                if (m instanceof HudModule h) {
                    if (o.has("x")) h.x = o.get("x").getAsInt();
                    if (o.has("y")) h.y = o.get("y").getAsInt();
                }
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.settings) {
                        if (!s.has(st.name)) continue;
                        try { st.load(s.get(st.name)); } catch (Exception ignored) { }
                    }
                }
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
            }
        } finally {
            loading = false;
        }
        Theme.apply();
    }

    // ---- main config ------------------------------------------------------------------------

    public static void save() {
        try {
            JsonObject root = snapshot();
            JsonObject meta = new JsonObject();
            meta.addProperty("profile", activeProfile);
            root.add(META, meta);
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            if (!Files.exists(path())) { Theme.apply(); return; }
            JsonObject root = JsonParser.parseString(Files.readString(path())).getAsJsonObject();
            if (root.has(META) && root.getAsJsonObject(META).has("profile"))
                activeProfile = root.getAsJsonObject(META).get("profile").getAsString();
            apply(root);
        } catch (Exception e) {
            e.printStackTrace();
            Theme.apply();
        }
    }

    // ---- profiles ---------------------------------------------------------------------------

    /** Letters, digits, space, '-' and '_' only; max 20 chars. Empty result = invalid. */
    public static String sanitize(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c) && c < 128 || c == ' ' || c == '-' || c == '_') sb.append(c);
        }
        String s = sb.toString().trim();
        return s.length() > 20 ? s.substring(0, 20).trim() : s;
    }

    public static boolean isBuiltin(String name) {
        for (String b : BUILTIN) if (b.equalsIgnoreCase(name)) return true;
        return false;
    }

    private static Path profilePath(String name) { return profileDir().resolve(name + ".json"); }

    public static List<String> profiles() {
        List<String> out = new ArrayList<>();
        try {
            if (!Files.isDirectory(profileDir())) return out;
            try (Stream<Path> st = Files.list(profileDir())) {
                st.map(p -> p.getFileName().toString())
                        .filter(n -> n.endsWith(".json"))
                        .map(n -> n.substring(0, n.length() - 5))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(out::add);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static boolean saveProfile(String rawName) {
        String name = sanitize(rawName);
        if (name.isEmpty() || isBuiltin(name)) return false;
        try {
            Files.createDirectories(profileDir());
            Files.writeString(profilePath(name), GSON.toJson(snapshot()));
            activeProfile = name;
            save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadProfile(String name) {
        try {
            if (isBuiltin(name)) { applyBuiltin(name); activeProfile = name; save(); return true; }
            Path p = profilePath(sanitize(name));
            if (!Files.exists(p)) return false;
            apply(JsonParser.parseString(Files.readString(p)).getAsJsonObject());
            activeProfile = name;
            save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteProfile(String name) {
        try {
            boolean ok = Files.deleteIfExists(profilePath(sanitize(name)));
            if (ok && activeProfile.equalsIgnoreCase(name)) { activeProfile = ""; save(); }
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void applyBuiltin(String name) {
        Set<String> on = new HashSet<>();
        switch (name) {
            case "PvP" -> on.addAll(Arrays.asList("FPS", "CPS", "Ping", "Keystrokes", "Armor Status", "Potion Status", "Combo"));
            case "Survival" -> on.addAll(Arrays.asList("FPS", "Coordinates", "Direction", "Clock", "Armor Status", "Hunger", "Toggle Sprint", "Zoom"));
            case "Performance" -> on.addAll(Arrays.asList("FPS", "Memory", "Performance Presets", "Particle Settings"));
            default -> on.add("FPS");
        }
        for (Module m : ModuleManager.all()) {
            // look-and-feel modules are left alone so switching profile never changes your theme
            if (m.category == com.victusclient.module.Category.CUSTOMIZE) continue;
            if (m.name.equals("Color Themes") || m.name.equals("Custom GUI")) continue;
            m.setEnabled(on.contains(m.name));
        }
        if (name.equals("Performance")) {
            PerformancePresetsModule pp = ModuleManager.of(PerformancePresetsModule.class);
            if (pp != null) pp.preset.index = 1; // Low
        }
    }
}
