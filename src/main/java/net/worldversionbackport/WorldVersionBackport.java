package net.worldversionbackport;

import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldVersionBackport implements ModInitializer {

    public static final String MOD_ID = "worldversionbackport";
    public static final String MOD_VERSION = "0.0.6";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // DataVersion of the running game — resolved at init from SharedConstants so it works for any MC version.
    public static int DATA_VERSION_CURRENT = 4189; // overwritten at onInitialize()

    public static final int DATA_VERSION_1_21_4 = 4189;

    // Anything above this is treated as a v26+ world (26.2-snapshot-1 threshold)
    public static final int NEW_FORMAT_THRESHOLD = 4200;

    @Override
    public void onInitialize() {
        DATA_VERSION_CURRENT = SharedConstants.getGameVersion().getSaveVersion().getId();
        LOGGER.info("WorldVersionBackport - Loaded. DataVersion={}", DATA_VERSION_CURRENT);
    }
}
