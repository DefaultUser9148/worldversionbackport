package net.worldversionbackport.screen;

import net.worldversionbackport.widget.WvbWorldListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.worldversionbackport.WorldPathTranslator;

import java.io.File;

public class DowngradeSelectScreen extends Screen {

    private final Screen parent;
    private final String preSelectedWorldName;

    private WvbWorldListWidget worldList;
    private ButtonWidget downgradeBtn;
    private ButtonWidget backupBtn;

    public DowngradeSelectScreen(Screen parent) {
        this(parent, null);
    }

    public DowngradeSelectScreen(Screen parent, String preSelectedWorldName) {
        super(Text.literal("Downgrade a World"));
        this.parent = parent;
        this.preSelectedWorldName = preSelectedWorldName;
    }

    @Override
    protected void init() {
        int listTop    = 32;
        int listBottom = height - 68;
        int listHeight = listBottom - listTop;

        worldList = new WvbWorldListWidget(
            client, width, listHeight, listTop, 20,
            client.getLevelStorage().getSavesDirectory(),
            path -> new WorldPathTranslator(path).isNewFormat(),
            entry -> updateButtons()
        );
        addDrawableChild(worldList);

        int cx = width / 2;
        int row1 = height - 60;
        int row2 = height - 36;

        backupBtn = ButtonWidget.builder(Text.literal("Backup World"), btn -> openBackup())
            .dimensions(cx - 100, row1, 96, 20).build();
        downgradeBtn = ButtonWidget.builder(Text.literal("Downgrade World"), btn -> confirmDowngrade())
            .dimensions(cx + 4, row1, 96, 20).build();

        addDrawableChild(downgradeBtn);
        addDrawableChild(backupBtn);

        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), btn -> {
            worldList.refresh();
            updateButtons();
        }).dimensions(cx - 100, row2, 96, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn ->
            client.setScreen(parent)
        ).dimensions(cx + 4, row2, 96, 20).build());

        updateButtons();

        if (preSelectedWorldName != null) {
            worldList.preselectByName(preSelectedWorldName);
        }
    }

    private void updateButtons() {
        boolean hasSelection = worldList != null && worldList.getSelectedOrNull() != null;
        if (downgradeBtn != null) downgradeBtn.active = hasSelection;
        if (backupBtn    != null) backupBtn.active    = hasSelection;
    }

    private void confirmDowngrade() {
        WvbWorldListWidget.WorldEntry entry = worldList.getSelectedOrNull();
        if (entry == null) return;

        client.setScreen(new AlphaWarningScreen(this, entry.name, confirmed -> {
            if (confirmed) {
                File worldFile = client.getLevelStorage().getSavesDirectory()
                    .resolve(entry.name).toFile();
                client.setScreen(new DowngradeProgressScreen(this, worldFile));
            } else {
                client.setScreen(this);
            }
        }));
    }

    private void openBackup() {
        WvbWorldListWidget.WorldEntry entry = worldList.getSelectedOrNull();
        if (entry == null) return;
        client.setScreen(new BackupConfirmScreen(this, entry.name));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);

        if (worldList != null && worldList.size() == 0) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("No newer-format worlds found in saves."),
                width / 2, height / 2, 0xAAAAAA);
        }
    }
}
