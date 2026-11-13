# Victus Client mod - multi-version (Fabric 1.20 -> 1.21.11)

One source tree, one jar per Minecraft range:

| jar                          | Minecraft          | code variant |
|------------------------------|--------------------|--------------|
| victusclient-1.20.1.jar      | 1.20 - 1.20.1      | v1200 (Java 17) |
| victusclient-1.20.2.jar      | 1.20.2             | v1202 (Java 17) |
| victusclient-1.20.4.jar      | 1.20.3 - 1.20.4    | v1202 (Java 17) |
| victusclient-1.20.6.jar      | 1.20.5 - 1.20.6    | legacy |
| victusclient-1.21.1.jar      | 1.21 - 1.21.1      | legacy       |
| victusclient-1.21.4.jar      | 1.21.2 - 1.21.4    | legacy       |
| victusclient-1.21.5.jar      | 1.21.5             | legacy       |
| victusclient-1.21.8.jar      | 1.21.6 - 1.21.8    | mid          |
| victusclient-1.21.11.jar     | 1.21.9 - 1.21.11   | modern       |

Right Shift in game -> Victus Mod Menu.

## New in v1.5 - Victus main menu + loading popup
- `VictusTitleScreen` replaces the vanilla title screen (Singleplayer, Multiplayer, Options, Victus Mods, Quit; Profiles / HUD Editor shortcuts).
- `VictusLoadingOverlay` replaces the Mojang loading screen with a Victus-branded popup, then fades into the menu.
- Hooks live in `mixin/MinecraftClientMixin.java`. To get the vanilla menu back for debugging, launch with `-Dvictusclient.vanillaTitle=true`.

## Features (v1.4)
Menu: left-click a card to toggle (or open settings if it has some), right-click to always open settings,
heart = favourite. Footer: **PROFILES** and **HUD EDITOR**.

| Group | What's in it |
|-------|--------------|
| HUD | FPS (optional avg/min/max), CPS, Ping, Coordinates, Keystrokes (WASD + Space + mouse), Armor Status, Direction, Clock, Memory (3 display modes) + more |
| Visual | Fullbright, Zoom (rebindable key, smooth), **Custom Crosshair** (5 styles, outline, dynamic gap), **Particle Settings**, **Color Themes** (7 presets), **Custom GUI** (menu size, columns, corners, backdrop) |
| Performance | **Performance Presets** (Potato..Quality, applied on enable/change only), **Render Distance** controls (+ FPS auto adjust) |
| Customize | **Custom Themes** (RGB accent + background tint), **Menu Animations** (open anim, animated switches, hover glow, smooth scroll), **Logo & Branding** (animated logo, custom name/tagline, optional HUD watermark) |
| Keybinds | Every module has a **Toggle Key** in its settings (click, press a key; Del clears; right-click clears). Zoom has its own **Zoom Key**. |
| Profiles | Save/load your own setups + built-in PvP / Survival / Minimal / Performance. Files live in `config/victusclient-profiles/`. |
| HUD Editor | Snap to edges / centre / other elements, arrow-key nudge, `G` toggles snap, `R` resets selected element |

Notes:
- Custom Crosshair can hide the vanilla crosshair on 1.21.6+ only (needs the HUD-element API). On older builds it draws on top.
- Motion blur is not included: it needs render-pipeline hooks that differ per Minecraft version.

## Build locally (one target)
    gradle wrapper --gradle-version 9.2.1      # once, JDK 21
    ./gradlew -Ptarget=1.21.4 build

## Build + release everything (GitHub Actions)
1. Push this folder to a GitHub repo.
2. `git tag v1.2.0 && git push origin v1.2.0`
3. The workflow builds every file in `versions/`, attaches all jars and `versions.json` to the release.

## Launcher
`launcher/victusModInstaller.js` reads
`https://github.com/<repo>/releases/latest/download/versions.json`, picks the jar for the profile's
Minecraft version, verifies SHA-256 and installs it (plus Fabric API) into the profile's mods folder.

## Add / split a version
Copy a file in `versions/`, change the 6 values (use https://fabricmc.net/develop for versions).
If a jar crashes on one Minecraft version of its range, give that version its own file.
