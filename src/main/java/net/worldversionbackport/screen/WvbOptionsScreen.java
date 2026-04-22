package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.worldversionbackport.WvbConfig;

public class WvbOptionsScreen extends Screen {

    private final Screen parent;

    public WvbOptionsScreen(Screen parent) {
        super(Text.literal("WorldVersionBackport - Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 30;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Suppress Downgrade Warning: " + (WvbConfig.get().suppressVanillaDowngradeWarning ? "ON" : "OFF")),
            btn -> {
                WvbConfig.get().suppressVanillaDowngradeWarning = !WvbConfig.get().suppressVanillaDowngradeWarning;
                WvbConfig.save();
                btn.setMessage(Text.literal("Suppress Downgrade Warning: " + (WvbConfig.get().suppressVanillaDowngradeWarning ? "ON" : "OFF")));
            }
        ).dimensions(cx - 150, y, 300, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Suppress Experimental Warning: " + (WvbConfig.get().suppressExperimentalWarning ? "ON" : "OFF")),
            btn -> {
                WvbConfig.get().suppressExperimentalWarning = !WvbConfig.get().suppressExperimentalWarning;
                WvbConfig.save();
                btn.setMessage(Text.literal("Suppress Experimental Warning: " + (WvbConfig.get().suppressExperimentalWarning ? "ON" : "OFF")));
            }
        ).dimensions(cx - 150, y, 300, 20).build());
        y += 30;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            btn -> client.setScreen(parent)
        ).dimensions(cx - 50, y, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 65, 0xFFFFFF);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
