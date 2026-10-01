package net.bunnyclient.mixin;

import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.gui.BunnyTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Swaps vanilla's TitleScreen for ours at the moment it is about to be shown. */
@Mixin(MinecraftClient.class)
public class TitleScreenMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen bunnyReplaceTitle(Screen screen) {
        if (screen != null
                && screen.getClass() == TitleScreen.class
                && ConfigManager.getConfig().lunarTitleScreen) {
            return new BunnyTitleScreen();
        }
        return screen;
    }
}
