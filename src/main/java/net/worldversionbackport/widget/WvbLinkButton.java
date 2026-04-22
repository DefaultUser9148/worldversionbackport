package net.worldversionbackport.widget;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.worldversionbackport.WorldVersionBackport;

import java.net.URI;

public class WvbLinkButton extends WvbIconButton {

    public WvbLinkButton(int x, int y, int size, Identifier texture, Text tooltip, String url) {
        super(x, y, size, texture, tooltip, btn -> {
            try {
                Util.getOperatingSystem().open(new URI(url));
            } catch (Exception e) {
                WorldVersionBackport.LOGGER.error("Failed to open link: {}", url, e);
            }
        });
    }
}
