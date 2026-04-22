package net.worldversionbackport.mixin.client;

import net.minecraft.world.level.storage.LevelSummary;
import net.worldversionbackport.WvbConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses the "This world uses experimental features" warning in SelectWorldScreen
 * by making LevelSummary.isExperimental() return false when the config toggle is on.
 */
@Mixin(LevelSummary.class)
public class SuppressExperimentalWarningMixin {

    @Inject(method = "isExperimental", at = @At("HEAD"), cancellable = true)
    private void suppressExperimental(CallbackInfoReturnable<Boolean> cir) {
        if (WvbConfig.get().suppressExperimentalWarning) {
            cir.setReturnValue(false);
        }
    }
}
