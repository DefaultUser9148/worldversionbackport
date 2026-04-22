package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.worldversionbackport.WorldLoadWarningState;

public class DowngradeCompleteScreen extends Screen {

    private final Screen parent;
    private final String worldName;
    private final WorldListWidget.WorldEntry pendingEntry;

    public DowngradeCompleteScreen(Screen parent, String worldName, WorldListWidget.WorldEntry pendingEntry) {
        super(Text.literal("Downgrading World Completed"));
        this.parent = parent;
        this.worldName = worldName;
        this.pendingEntry = pendingEntry;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 + 10;

        addDrawableChild(ButtonWidget.builder(Text.literal("Yes"), btn -> {
            if (pendingEntry != null) {
                WorldLoadWarningState.bypassSet.add(worldName);
                pendingEntry.play();
            } else {
                client.createIntegratedServerLoader().start(worldName, () -> client.setScreen(this));
            }
        }).dimensions(cx - 102, y, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("No"), btn ->
            client.setScreen(parent)
        ).dimensions(cx + 2, y, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 20;

        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, y, 0xFFFFFF);
        y += 14;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Do you want to join the world now?"), cx, y, 0xAAAAAA);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
