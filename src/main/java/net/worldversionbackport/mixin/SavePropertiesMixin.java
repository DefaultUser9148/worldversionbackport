package net.worldversionbackport.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.level.storage.LevelStorage;
import net.worldversionbackport.LevelDatTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(LevelStorage.class)
public class SavePropertiesMixin {

    @Inject(
        method = "readLevelProperties(Ljava/nio/file/Path;)Lnet/minecraft/nbt/NbtCompound;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void onReadRawLevelProperties(Path path,
                                                  CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound root = cir.getReturnValue();
        if (root == null) return;

        if (!root.contains("Data", NbtElement.COMPOUND_TYPE)) return;
        NbtCompound data = root.getCompound("Data");

        if (!LevelDatTranslator.isNewFormat(data)) return;

        // path is the level.dat file; parent is the world root
        Path worldRoot = path.getParent();

        NbtCompound dataCopy = data.copy();
        LevelDatTranslator.translate(dataCopy, worldRoot);

        NbtCompound rootCopy = root.copy();
        rootCopy.put("Data", dataCopy);

        cir.setReturnValue(rootCopy);
    }
}
