package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.worldversionbackport.WorldVersionBackport;

public class BackupConfirmScreen extends Screen {

    private static final String[] LINES = {
        "The backup process may take a while.",
        "Do NOT interrupt it. Do NOT kill Minecraft.",
        "It can seriously corrupt your worlds.",
        "Do not take chances.",
        "",
        "A separate screen may show after — the 'Downgrade Unsupported'",
        "screen with the classic Minecraft warning. It's there for a reason.",
        "If you make a backup using THIS screen, you can ignore the other one,",
        "as it uses the same function."
    };

    private final Screen parent;
    private final String levelName;
    /** If non-null, shown as a "Load World" button after a successful backup. */
    private final Runnable afterBackup;

    private String statusMessage = null;
    private boolean done = false;
    private ButtonWidget loadWorldBtn = null;

    public BackupConfirmScreen(Screen parent, String levelName) {
        this(parent, levelName, null);
    }

    public BackupConfirmScreen(Screen parent, String levelName, Runnable afterBackup) {
        super(Text.literal("Backup World - WorldVersionBackport"));
        this.parent = parent;
        this.levelName = levelName;
        this.afterBackup = afterBackup;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int by = height / 2 + 60;

        addDrawableChild(ButtonWidget.builder(Text.literal("Proceed with Backup"), btn -> doBackup())
            .dimensions(cx - 105, by, 210, 20).build());

        if (afterBackup != null) {
            loadWorldBtn = ButtonWidget.builder(Text.literal("Load World"), btn -> afterBackup.run())
                .dimensions(cx - 105, by + 24, 210, 20).build();
            loadWorldBtn.visible = false;
            addDrawableChild(loadWorldBtn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn ->
            client.setScreen(parent)
        ).dimensions(cx - 50, by + (afterBackup != null ? 48 : 24), 100, 20).build());
    }

    private void doBackup() {
        try (LevelStorage.Session session = client.getLevelStorage().createSession(levelName)) {
            long bytes = session.createBackup();
            statusMessage = "Backup complete! (" + (bytes / 1024) + " KB)";
            done = true;
            if (loadWorldBtn != null) loadWorldBtn.visible = true;
        } catch (Exception e) {
            statusMessage = "Backup failed: " + e.getMessage();
            WorldVersionBackport.LOGGER.error("Backup failed for {}", levelName, e);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 70;

        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("! Read before proceeding !"), cx, y, 0xFFFF55);
        y += 16;

        for (String line : LINES) {
            if (!line.isEmpty()) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(line), cx, y, 0xFFFFFF);
            }
            y += 11;
        }

        if (statusMessage != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(statusMessage),
                cx, height / 2 + 90, done ? 0x55FF55 : 0xFF5555);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
