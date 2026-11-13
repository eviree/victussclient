package com.victusclient.mixin;

import com.victusclient.gui.VictusLoadingOverlay;
import com.victusclient.gui.VictusTitleScreen;
import com.victusclient.gui.VictusPauseScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Swaps the vanilla main menu for the Victus one and wraps the first (startup) loading overlay with
 * the Victus loading popup. Launch with -Dvictusclient.vanillaTitle=true to get the vanilla menu back.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true, require = 1)
    private Screen victusclient$replaceTitle(Screen screen) {
        if (Boolean.getBoolean("victusclient.vanillaTitle")) return screen;
        MinecraftClient self = (MinecraftClient) (Object) this;
        // vanilla turns setScreen(null) into a new TitleScreen when no world is loaded - catch that too
        if (screen == null && self.world == null) return new VictusTitleScreen();
        if (screen != null && screen.getClass() == GameMenuScreen.class) return new VictusPauseScreen();
        if (screen != null && screen.getClass() == TitleScreen.class) return new VictusTitleScreen();
        return screen;
    }

    @ModifyVariable(method = "setOverlay", at = @At("HEAD"), argsOnly = true, require = 0)
    private Overlay victusclient$wrapLoading(Overlay overlay) {
        if (Boolean.getBoolean("victusclient.vanillaTitle")) return overlay;
        if (overlay instanceof SplashOverlay && VictusLoadingOverlay.claim()) return new VictusLoadingOverlay(overlay);
        return overlay;
    }
}
