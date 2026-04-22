package net.worldversionbackport.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import net.worldversionbackport.WorldLoadWarningState;
import net.worldversionbackport.WorldPathTranslator;
import net.worldversionbackport.screen.WorldFormatChoiceScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(WorldListWidget.WorldEntry.class)
public class WorldLoadWarningMixin {

    @Shadow @Final
    LevelSummary level;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void interceptPlay(CallbackInfo ci) {
        if (WorldLoadWarningState.bypassSet.remove(level.getName())) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Path savesDir = client.getLevelStorage().getSavesDirectory();
        Path worldDir = savesDir.resolve(level.getName());

        if (!new WorldPathTranslator(worldDir).isNewFormat()) return;

        ci.cancel();

        Screen selectScreen = client.currentScreen;
        WorldListWidget.WorldEntry self = (WorldListWidget.WorldEntry)(Object) this;
        WorldLoadWarningState.pendingAutoPlayEntry = self;

        Runnable proceedWithoutDowngrade = () -> {
            WorldLoadWarningState.bypassSet.add(level.getName());
            client.setScreen(selectScreen);
            self.play();
        };

        client.setScreen(new WorldFormatChoiceScreen(selectScreen, level.getName(), proceedWithoutDowngrade));
    }
}
