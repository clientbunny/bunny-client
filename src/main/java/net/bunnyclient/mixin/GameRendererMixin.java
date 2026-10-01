package net.bunnyclient.mixin;

import net.bunnyclient.features.FullbrightFeature;
import net.bunnyclient.features.ZoomFeature;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (ZoomFeature.isZooming()) {
            float originalFov = cir.getReturnValue();
            float modifiedFov = (float) ZoomFeature.modifyFov(originalFov);
            cir.setReturnValue(modifiedFov);
        }
    }

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void onGetNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (FullbrightFeature.isEnabled()) {
            cir.setReturnValue(1.0f);
        }
    }
}
