package net.worldversionbackport.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.worldversionbackport.WorldVersionBackport;
import net.worldversionbackport.WorldPathTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageMixin {

    @Shadow
    public abstract Path getDirectory(WorldSavePath savePath);

    @Inject(method = "getDirectory(Lnet/minecraft/util/WorldSavePath;)Ljava/nio/file/Path;",
            at = @At("RETURN"),
            cancellable = true)
    private void redirectWorldSavePath(WorldSavePath savePath,
                                       CallbackInfoReturnable<Path> cir) {
        Path original = cir.getReturnValue();
        if (original == null) return;

        Path worldRoot = original.getParent();
        if (worldRoot == null) return;

        WorldPathTranslator translator = new WorldPathTranslator(worldRoot);
        if (!translator.isNewFormat()) return;

        Path relative = worldRoot.relativize(original);
        Path translated = translator.resolve(relative);

        if (!translated.equals(original)) {
            WorldVersionBackport.LOGGER.debug(
                "[WorldVersionBackport] Redirecting path: {} -> {}", original, translated
            );
            cir.setReturnValue(translated);
        }
    }

    @Inject(method = "getWorldDirectory(Lnet/minecraft/registry/RegistryKey;)Ljava/nio/file/Path;",
            at = @At("RETURN"),
            cancellable = true)
    private void redirectWorldDirectory(RegistryKey<World> worldKey,
                                        CallbackInfoReturnable<Path> cir) {
        Path original = cir.getReturnValue();
        if (original == null) return;

        // For overworld, getWorldDirectory returns the world root itself.
        // For nether/end it returns a subdir. Check both the path and its parent for level.dat.
        Path worldRoot = findWorldRoot(original);
        if (worldRoot == null) return;

        WorldPathTranslator translator = new WorldPathTranslator(worldRoot);
        if (!translator.isNewFormat()) return;

        // Map the registry key namespace:path to the v26 dimension folder
        String namespace = worldKey.getValue().getNamespace();
        String path = worldKey.getValue().getPath();
        Path translated = translator.resolveWorldDirectory(namespace, path);

        if (translated != null && !translated.equals(original)) {
            WorldVersionBackport.LOGGER.debug(
                "[WorldVersionBackport] Redirecting world directory {}: {} -> {}",
                worldKey.getValue(), original, translated
            );
            cir.setReturnValue(translated);
        }
    }

    private static Path findWorldRoot(Path path) {
        // Check the path itself first (overworld case where path IS the world root)
        if (path.resolve("level.dat").toFile().exists()) return path;
        // Then walk up (nether/end case where path is a subdir)
        Path candidate = path.getParent();
        int maxDepth = 5;
        while (candidate != null && maxDepth-- > 0) {
            if (candidate.resolve("level.dat").toFile().exists()) return candidate;
            candidate = candidate.getParent();
        }
        return null;
    }
}
