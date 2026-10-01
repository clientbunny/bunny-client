package net.bunnyclient.mixin;

import net.bunnyclient.config.ConfigManager;
import net.bunnyclient.util.BunnyUsers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    // Single glyph (U+E000) in assets/bunnyclient/font/badge.json -> bunny head icon
    private static final Style BADGE_STYLE = Style.EMPTY
            .withFont(new StyleSpriteSource.Font(Identifier.of("bunnyclient", "badge")));

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void addBunnyBadge(CallbackInfoReturnable<Text> cir) {
        if (!ConfigManager.getConfig().bunnyBadge) return;
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!BunnyUsers.isBunnyUser(self.getUuid())) return;

        Text original = cir.getReturnValue();
        if (original == null) return;
        Text badge = Text.literal("\uE000").setStyle(BADGE_STYLE);
        cir.setReturnValue(Text.empty().append(badge).append(Text.literal(" ")).append(original));
    }
}
