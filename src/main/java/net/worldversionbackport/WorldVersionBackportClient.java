package net.worldversionbackport;

import net.fabricmc.api.ClientModInitializer;

public class WorldVersionBackportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WvbConfig.get();
    }
}
