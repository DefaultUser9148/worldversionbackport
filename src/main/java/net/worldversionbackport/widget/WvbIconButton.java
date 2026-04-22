package net.worldversionbackport.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WvbIconButton extends ButtonWidget {

    private final Identifier texture;
    private final int iconSize;

    public WvbIconButton(int x, int y, int size, Identifier texture, Text tooltip, PressAction onPress) {
        super(x, y, size, size, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
        this.iconSize = size - 4;
        if (!tooltip.getString().isEmpty()) {
            setTooltip(Tooltip.of(tooltip));
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        int iconX = getX() + (width - iconSize) / 2;
        int iconY = getY() + (height - iconSize) / 2;
        context.drawTexture(RenderLayer::getGuiTextured, texture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }
}
