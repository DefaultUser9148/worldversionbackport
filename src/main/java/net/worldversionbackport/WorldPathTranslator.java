package net.worldversionbackport;

import java.nio.file.Files;
import java.nio.file.Path;

public class WorldPathTranslator {

    private final Path worldRoot;
    private final boolean isNewFormat;

    public WorldPathTranslator(Path worldRoot) {
        this.worldRoot = worldRoot;
        this.isNewFormat = detectNewFormat(worldRoot);
    }

    private static boolean detectNewFormat(Path worldRoot) {
        return Files.isDirectory(worldRoot.resolve("dimensions").resolve("minecraft").resolve("overworld"));
    }

    public boolean isNewFormat() {
        return isNewFormat;
    }

    public Path resolve(Path relative) {
        if (!isNewFormat) return worldRoot.resolve(relative);
        return translate(relative.toString().replace('\\', '/'));
    }

    /**
     * Resolves the dimension directory for getWorldDirectory() calls.
     * In v26 format all dimensions live under dimensions/<namespace>/<path>/.
     * In 1.21.4 format: overworld = world root, nether = DIM-1, end = DIM1.
     * Returns null if no redirect is needed.
     */
    public Path resolveWorldDirectory(String namespace, String path) {
        if (!isNewFormat) return null;
        if ("minecraft".equals(namespace)) {
            switch (path) {
                case "overworld":
                    return worldRoot.resolve("dimensions/minecraft/overworld");
                case "the_nether":
                    return worldRoot.resolve("dimensions/minecraft/the_nether");
                case "the_end":
                    return worldRoot.resolve("dimensions/minecraft/the_end");
            }
        }
        // Custom dimensions (e.g. midwut:main) live at dimensions/<namespace>/<path>
        return worldRoot.resolve("dimensions").resolve(namespace).resolve(path);
    }

    private Path translate(String rel) {
        if (rel.equals("region") || rel.startsWith("region/"))
            return worldRoot.resolve("dimensions/minecraft/overworld").resolve(rel);
        if (rel.equals("entities") || rel.startsWith("entities/"))
            return worldRoot.resolve("dimensions/minecraft/overworld").resolve(rel);
        if (rel.equals("poi") || rel.startsWith("poi/"))
            return worldRoot.resolve("dimensions/minecraft/overworld").resolve(rel);
        if (rel.equals("DIM-1") || rel.startsWith("DIM-1/")) {
            String rest = rel.equals("DIM-1") ? "" : rel.substring("DIM-1/".length());
            return worldRoot.resolve("dimensions/minecraft/the_nether").resolve(rest);
        }
        if (rel.equals("DIM1") || rel.startsWith("DIM1/")) {
            String rest = rel.equals("DIM1") ? "" : rel.substring("DIM1/".length());
            return worldRoot.resolve("dimensions/minecraft/the_end").resolve(rest);
        }
        if (rel.equals("advancements") || rel.startsWith("advancements/"))
            return worldRoot.resolve("players").resolve(rel);
        if (rel.equals("playerdata") || rel.startsWith("playerdata/")) {
            String rest = rel.equals("playerdata") ? "" : rel.substring("playerdata/".length());
            Path base = worldRoot.resolve("players/data");
            return rest.isEmpty() ? base : base.resolve(rest);
        }
        if (rel.equals("stats") || rel.startsWith("stats/"))
            return worldRoot.resolve("players").resolve(rel);
        if (rel.equals("data/scoreboard.dat"))
            return worldRoot.resolve("data/minecraft/scoreboard.dat");
        if (rel.equals("data/idcounts.dat"))
            return worldRoot.resolve("data/minecraft/last_id.dat");
        if (rel.equals("data/maps") || rel.startsWith("data/maps/")) {
            String rest = rel.equals("data/maps") ? "" : rel.substring("data/maps/".length());
            Path base = worldRoot.resolve("data/minecraft/maps");
            return rest.isEmpty() ? base : base.resolve(rest);
        }
        if (rel.contains("/structures/"))
            return worldRoot.resolve(rel.replace("/structures/", "/structure/"));
        if (rel.equals("resources.zip"))
            return worldRoot.resolve("resourcepacks/resources.zip");

        return worldRoot.resolve(rel);
    }
}
