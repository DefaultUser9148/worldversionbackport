package net.worldversionbackport;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class DowngradeHandler {

    public static final int TOTAL_STAGES = 5;

    public interface ProgressListener {
        void update(int stage, String stageName, int filesMoved, int filesTotal);
    }

    public static void downgrade(Path worldRoot) throws IOException {
        downgrade(worldRoot, null);
    }

    public static void downgrade(Path worldRoot, ProgressListener progress) throws IOException {

        // Stage 1: Overworld
        Path overworld = worldRoot.resolve("dimensions/minecraft/overworld");
        if (Files.isDirectory(overworld)) {
            int total = 0;
            for (String sub : new String[]{"region", "entities", "poi", "data"}) {
                total += countFiles(overworld.resolve(sub));
            }
            int finalTotal = total;
            int[] moved = {0};
            report(progress, 1, "Overworld", 0, finalTotal);
            for (String sub : new String[]{"region", "entities", "poi", "data"}) {
                Path src = overworld.resolve(sub);
                if (Files.exists(src)) {
                    moveDir(src, worldRoot.resolve(sub), () -> {
                        moved[0]++;
                        report(progress, 1, "Overworld", moved[0], finalTotal);
                    });
                }
            }
        } else {
            report(progress, 1, "Overworld", 0, 0);
        }

        // Stage 2: Nether
        Path nether = worldRoot.resolve("dimensions/minecraft/the_nether");
        if (Files.isDirectory(nether)) {
            int total = countFiles(nether);
            int[] moved = {0};
            report(progress, 2, "Nether", 0, total);
            moveDir(nether, worldRoot.resolve("DIM-1"), () -> {
                moved[0]++;
                report(progress, 2, "Nether", moved[0], total);
            });
        } else {
            report(progress, 2, "Nether", 0, 0);
        }

        // Stage 3: The End
        Path end = worldRoot.resolve("dimensions/minecraft/the_end");
        if (Files.isDirectory(end)) {
            int total = countFiles(end);
            int[] moved = {0};
            report(progress, 3, "The End", 0, total);
            moveDir(end, worldRoot.resolve("DIM1"), () -> {
                moved[0]++;
                report(progress, 3, "The End", moved[0], total);
            });
        } else {
            report(progress, 3, "The End", 0, 0);
        }

        // Stage 4: Player data
        int playerTotal = countFiles(worldRoot.resolve("players/data"))
            + countFiles(worldRoot.resolve("players/advancements"))
            + countFiles(worldRoot.resolve("players/stats"));
        int[] playerMoved = {0};
        report(progress, 4, "Player Data", 0, playerTotal);
        Runnable playerTick = () -> { playerMoved[0]++; report(progress, 4, "Player Data", playerMoved[0], playerTotal); };
        moveDir(worldRoot.resolve("players/data"),         worldRoot.resolve("playerdata"),   playerTick);
        moveDir(worldRoot.resolve("players/advancements"), worldRoot.resolve("advancements"), playerTick);
        moveDir(worldRoot.resolve("players/stats"),        worldRoot.resolve("stats"),        playerTick);

        // Stage 5: World data files
        report(progress, 5, "World Data", 0, 0);
        Path mcData = worldRoot.resolve("data/minecraft");
        if (Files.isDirectory(mcData)) {
            moveSingle(mcData.resolve("scoreboard.dat"), worldRoot.resolve("data/scoreboard.dat"));
            moveSingle(mcData.resolve("last_id.dat"),    worldRoot.resolve("data/idcounts.dat"));
            Path mcMaps = mcData.resolve("maps");
            if (Files.isDirectory(mcMaps)) moveDir(mcMaps, worldRoot.resolve("data/maps"), null);
        }
        report(progress, 5, "World Data", 1, 1);

        // Cleanup empty dirs
        deleteIfEmpty(worldRoot.resolve("dimensions/minecraft/overworld"));
        deleteIfEmpty(worldRoot.resolve("dimensions/minecraft"));

        // Write back translated level.dat so Minecraft sees DataVersion = 1.21.4 (removes red text)
        translateAndWriteLevelDat(worldRoot);

        WorldVersionBackport.LOGGER.info("WorldVersionBackport - Downgraded.");
    }

    private static void report(ProgressListener p, int stage, String name, int moved, int total) {
        if (p != null) p.update(stage, name, moved, total);
    }

    private static int countFiles(Path dir) {
        if (!Files.isDirectory(dir)) return 0;
        try (Stream<Path> s = Files.walk(dir)) {
            return (int) s.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private static void moveDir(Path src, Path dst, Runnable onEachFile) throws IOException {
        if (!Files.exists(src)) return;
        Files.createDirectories(dst);
        try (Stream<Path> entries = Files.list(src)) {
            for (Path entry : (Iterable<Path>) entries::iterator) {
                Path target = dst.resolve(src.relativize(entry));
                if (Files.isDirectory(entry)) {
                    moveDir(entry, target, onEachFile);
                } else {
                    Files.move(entry, target, StandardCopyOption.REPLACE_EXISTING);
                    if (onEachFile != null) onEachFile.run();
                }
            }
        }
        Files.deleteIfExists(src);
    }

    private static void moveSingle(Path src, Path dst) throws IOException {
        if (!Files.exists(src)) return;
        Files.createDirectories(dst.getParent());
        Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void translateAndWriteLevelDat(Path worldRoot) {
        Path levelDat = worldRoot.resolve("level.dat");
        if (!Files.exists(levelDat)) return;
        try {
            NbtCompound root = NbtIo.readCompressed(levelDat, NbtSizeTracker.ofUnlimitedBytes());
            if (!root.contains("Data", NbtElement.COMPOUND_TYPE)) return;
            NbtCompound rawData = root.getCompound("Data");
            if (rawData == null) return;
            NbtCompound data = rawData.copy();
            if (!LevelDatTranslator.isNewFormat(data)) return; // already translated
            LevelDatTranslator.translate(data, worldRoot);
            root.put("Data", data);
            NbtIo.writeCompressed(root, levelDat);
        } catch (IOException e) {
            WorldVersionBackport.LOGGER.error("WorldVersionBackport - Failed to write level.dat: {}", e.getMessage());
        }
    }

    private static void deleteIfEmpty(Path dir) {
        try {
            if (Files.isDirectory(dir)) {
                try (Stream<Path> s = Files.list(dir)) {
                    if (s.findAny().isEmpty()) Files.delete(dir);
                }
            }
        } catch (IOException ignored) {}
    }
}
