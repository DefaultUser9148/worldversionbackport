package net.worldversionbackport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class WvbConfig {

    public static volatile String pendingAutoJoin = null;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("worldversionbackport.json");

    private static Data instance = null;

    public static class Data {
        public boolean suppressVanillaDowngradeWarning = true;
        public boolean suppressExperimentalWarning = true;
    }

    public static Data get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = CONFIG_PATH.toFile();
        if (f.exists()) {
            try (Reader r = new FileReader(f)) {
                instance = GSON.fromJson(r, Data.class);
                if (instance == null) instance = new Data();
            } catch (Exception e) {
                instance = new Data();
            }
        } else {
            instance = new Data();
            save();
        }
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            WorldVersionBackport.LOGGER.error("Failed to save WvbConfig", e);
        }
    }
}
