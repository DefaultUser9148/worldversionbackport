package net.worldversionbackport;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtSizeTracker;

import java.io.IOException;
import java.nio.file.Path;

public class LevelDatTranslator {

    private static final int DIFFICULTY_PEACEFUL = 0;
    private static final int DIFFICULTY_EASY     = 1;
    private static final int DIFFICULTY_NORMAL   = 2;
    private static final int DIFFICULTY_HARD     = 3;

    public static boolean isNewFormat(NbtCompound data) {
        return data.getInt("DataVersion") > WorldVersionBackport.NEW_FORMAT_THRESHOLD;
    }

    public static void translate(NbtCompound data, Path worldRoot) {
        translateDataVersion(data);
        translateVersionCompound(data);
        translateSpawn(data);
        translateDifficultySettings(data);
        translateWanderingTrader(data);
        injectWorldGenSettings(data, worldRoot);
        injectMissingFields(data);
    }

    private static void translateDataVersion(NbtCompound data) {
        data.putInt("DataVersion", WorldVersionBackport.DATA_VERSION_1_21_4);
    }

    private static void translateVersionCompound(NbtCompound data) {
        NbtCompound version = new NbtCompound();
        version.putInt("Id", WorldVersionBackport.DATA_VERSION_1_21_4);
        version.putString("Name", "1.21.4");
        version.putString("Series", "main");
        version.putByte("Snapshot", (byte) 0);
        data.put("Version", version);
    }

    private static void translateSpawn(NbtCompound data) {
        if (!data.contains("spawn", NbtElement.COMPOUND_TYPE)) return;
        NbtCompound spawn = data.getCompound("spawn");

        if (spawn.contains("pos", NbtElement.INT_ARRAY_TYPE)) {
            int[] pos = spawn.getIntArray("pos");
            if (pos.length >= 3) {
                data.putInt("SpawnX", pos[0]);
                data.putInt("SpawnY", pos[1]);
                data.putInt("SpawnZ", pos[2]);
            }
        }
        data.putFloat("SpawnAngle", spawn.contains("yaw", NbtElement.FLOAT_TYPE)
                ? spawn.getFloat("yaw") : 0.0f);
        data.remove("spawn");
    }

    private static void translateDifficultySettings(NbtCompound data) {
        if (!data.contains("difficulty_settings", NbtElement.COMPOUND_TYPE)) return;
        NbtCompound ds = data.getCompound("difficulty_settings");

        if (ds.contains("difficulty", NbtElement.STRING_TYPE))
            data.putByte("Difficulty", (byte) difficultyStringToByte(ds.getString("difficulty")));
        if (ds.contains("hardcore", NbtElement.BYTE_TYPE))
            data.putByte("hardcore", ds.getByte("hardcore"));
        if (ds.contains("locked", NbtElement.BYTE_TYPE))
            data.putByte("DifficultyLocked", ds.getByte("locked"));

        data.remove("difficulty_settings");
    }

    private static int difficultyStringToByte(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "peaceful" -> DIFFICULTY_PEACEFUL;
            case "easy"     -> DIFFICULTY_EASY;
            case "normal"   -> DIFFICULTY_NORMAL;
            case "hard"     -> DIFFICULTY_HARD;
            default         -> DIFFICULTY_EASY;
        };
    }

    private static void translateWanderingTrader(NbtCompound data) {
        if (!data.contains("singleplayer_uuid", NbtElement.INT_ARRAY_TYPE)) return;
        data.put("WanderingTraderId", new NbtIntArray(data.getIntArray("singleplayer_uuid")));
        data.remove("singleplayer_uuid");
    }

    private static void injectWorldGenSettings(NbtCompound data, Path worldRoot) {
        if (data.contains("WorldGenSettings", NbtElement.COMPOUND_TYPE)) return;

        // v26 moved WorldGenSettings out of level.dat into data/minecraft/world_gen_settings.dat
        // Try to read the real seed and dimension configs from there first
        NbtCompound wgsData = readWorldGenSettings(worldRoot);

        NbtCompound wgs = new NbtCompound();

        if (wgsData != null) {
            long seed = wgsData.contains("seed", NbtElement.LONG_TYPE) ? wgsData.getLong("seed") : 0L;
            wgs.put("seed", NbtLong.of(seed));
            wgs.putByte("bonus_chest", wgsData.contains("bonus_chest") ? wgsData.getByte("bonus_chest") : (byte) 0);
            wgs.putByte("generate_features", wgsData.contains("generate_structures") ? wgsData.getByte("generate_structures") : (byte) 1);

            if (wgsData.contains("dimensions", NbtElement.COMPOUND_TYPE)) {
                wgs.put("dimensions", wgsData.getCompound("dimensions").copy());
            } else {
                wgs.put("dimensions", buildDefaultDimensions());
            }
        } else {
            wgs.put("seed", NbtLong.of(0L));
            wgs.putByte("bonus_chest", (byte) 0);
            wgs.putByte("generate_features", (byte) 1);
            wgs.put("dimensions", buildDefaultDimensions());
        }

        data.put("WorldGenSettings", wgs);
    }

    private static NbtCompound readWorldGenSettings(Path worldRoot) {
        if (worldRoot == null) return null;
        Path wgsPath = worldRoot.resolve("data").resolve("minecraft").resolve("world_gen_settings.dat");
        if (!wgsPath.toFile().exists()) return null;

        try {
            NbtCompound root = NbtIo.readCompressed(wgsPath, NbtSizeTracker.ofUnlimitedBytes());
            // File structure: root compound contains "data" sub-compound with the actual settings
            if (root.contains("data", NbtElement.COMPOUND_TYPE)) {
                return root.getCompound("data");
            }
            return root;
        } catch (IOException e) {
            return null;
        }
    }

    private static NbtCompound buildDefaultDimensions() {
        NbtCompound dimensions = new NbtCompound();
        dimensions.put("minecraft:overworld", buildDimension(
                "minecraft:overworld", "minecraft:noise", "minecraft:multi_noise", "minecraft:overworld"));
        dimensions.put("minecraft:the_nether", buildDimension(
                "minecraft:the_nether", "minecraft:noise", "minecraft:multi_noise", "minecraft:nether"));
        dimensions.put("minecraft:the_end", buildDimension(
                "minecraft:the_end", "minecraft:noise", "minecraft:the_end", "minecraft:end"));
        return dimensions;
    }

    private static NbtCompound buildDimension(String dimensionType, String generatorType,
                                               String biomeSourceType, String biomeSourcePreset) {
        NbtCompound dim = new NbtCompound();
        dim.putString("type", dimensionType);

        NbtCompound generator = new NbtCompound();
        generator.putString("type", generatorType);
        generator.putString("settings", biomeSourcePreset);

        NbtCompound biomeSource = new NbtCompound();
        biomeSource.putString("type", biomeSourceType);
        if (biomeSourceType.equals("minecraft:multi_noise")) {
            biomeSource.putString("preset", biomeSourcePreset);
        }
        generator.put("biome_source", biomeSource);

        dim.put("generator", generator);
        return dim;
    }

    private static void injectMissingFields(NbtCompound data) {
        putIfMissing(data, "BorderCenterX",        NbtDouble.of(0.0));
        putIfMissing(data, "BorderCenterZ",        NbtDouble.of(0.0));
        putIfMissing(data, "BorderSize",           NbtDouble.of(59999968.0));
        putIfMissing(data, "BorderSizeLerpTarget", NbtDouble.of(59999968.0));
        putIfMissing(data, "BorderSizeLerpTime",   NbtLong.of(0L));
        putIfMissing(data, "BorderDamagePerBlock", NbtDouble.of(0.2));
        putIfMissing(data, "BorderSafeZone",       NbtDouble.of(5.0));
        putIfMissing(data, "BorderWarningBlocks",  NbtDouble.of(5.0));
        putIfMissing(data, "BorderWarningTime",    NbtDouble.of(15.0));
        putIfMissing(data, "DayTime",              NbtLong.of(6000L));
        putIfMissing(data, "clearWeatherTime",     NbtInt.of(0));
        putIfMissing(data, "rainTime",             NbtInt.of(20000));
        putIfMissing(data, "raining",              NbtByte.of((byte) 0));
        putIfMissing(data, "thunderTime",          NbtInt.of(20000));
        putIfMissing(data, "thundering",           NbtByte.of((byte) 0));
        putIfMissing(data, "WanderingTraderSpawnChance", NbtInt.of(75));
        putIfMissing(data, "WanderingTraderSpawnDelay",  NbtInt.of(0));

        if (!data.contains("GameRules",       NbtElement.COMPOUND_TYPE)) data.put("GameRules",       new NbtCompound());
        if (!data.contains("CustomBossEvents",NbtElement.COMPOUND_TYPE)) data.put("CustomBossEvents",new NbtCompound());
        if (!data.contains("DragonFight",     NbtElement.COMPOUND_TYPE)) data.put("DragonFight",     new NbtCompound());
        if (!data.contains("ScheduledEvents"))                            data.put("ScheduledEvents", new NbtList());
    }

    private static void putIfMissing(NbtCompound nbt, String key, NbtElement value) {
        if (!nbt.contains(key)) nbt.put(key, value);
    }
}
