package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.worldversionbackport.DowngradeHandler;
import net.worldversionbackport.WorldVersionBackport;
import net.worldversionbackport.WorldLoadWarningState;

import java.io.File;

public class DowngradeProgressScreen extends Screen {

    private final Screen parent;
    private final File world;
    private final String worldName;

    private volatile int stage = 0;
    private volatile String stageName = "Scanning files...";
    private volatile int filesMoved = 0;
    private volatile int filesTotal = 0;
    private volatile boolean done = false;
    private volatile String error = null;

    private volatile boolean navigated = false;
    private volatile boolean errorHandled = false;
    private ButtonWidget closeButton;

    public DowngradeProgressScreen(Screen parent, File world) {
        super(Text.literal("Downgrading World Files"));
        this.parent = parent;
        this.world = world;
        this.worldName = world.getName();
    }

    @Override
    protected void init() {
        closeButton = ButtonWidget.builder(Text.literal("Cancel"), btn -> client.setScreen(parent))
            .dimensions(width / 2 - 75, height - 52, 150, 20).build();
        closeButton.active = false;
        addDrawableChild(closeButton);

        Thread t = new Thread(() -> {
            try {
                DowngradeHandler.downgrade(world.toPath(), (s, name, moved, total) -> {
                    stage = s;
                    stageName = name;
                    filesMoved = moved;
                    filesTotal = total;
                });
                done = true;
            } catch (Exception e) {
                error = e.getMessage();
                WorldVersionBackport.LOGGER.error("Downgrade failed for {}", worldName, e);
            }
        }, "WVB-Downgrade");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void tick() {
        if (done && !navigated) {
            navigated = true;
            autoJoin();
        } else if (error != null && !errorHandled) {
            errorHandled = true;
            closeButton.active = true;
            closeButton.setMessage(Text.literal("Close"));
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 60;

        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, y, 0xFFFFFF);
        y += 18;

        if (error != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Error: " + error), cx, y, 0xFF5555);
            return;
        }

        drawProgressBar(ctx, cx, y, 220, 5, (float) stage / DowngradeHandler.TOTAL_STAGES, 0xFF55FF55);
        y += 9;

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Moved: " + filesMoved), cx, y, 0xFFFFFF);
        y += 11;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Total: " + filesTotal), cx, y, 0xFFFFFF);
        y += 11;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Stage: " + stage + "/" + DowngradeHandler.TOTAL_STAGES), cx, y, 0xFFFFFF);
        y += 18;

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal(stageName.isEmpty() ? "Scanning files..." : stageName), cx, y, 0xAAAAAA);
        y += 14;

        float fileProgress = filesTotal > 0 ? (float) filesMoved / filesTotal : 0f;
        drawProgressBar(ctx, cx, y, 220, 5, fileProgress, 0xFF888888);
        y += 9;

        int pct = filesTotal > 0 ? (int)(fileProgress * 100) : 0;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(pct + "%"), cx, y, 0xFFFFFF);
    }

    private static void drawProgressBar(DrawContext ctx, int cx, int y, int w, int h, float progress, int color) {
        int x0 = cx - w / 2;
        int x1 = cx + w / 2;
        ctx.fill(x0, y, x1, y + h, 0x88000000);
        int filled = Math.max(0, Math.min(w, (int)(progress * w)));
        if (filled > 0) ctx.fill(x0, y, x0 + filled, y + h, color);
    }

    private void autoJoin() {
        WorldListWidget.WorldEntry entry = WorldLoadWarningState.pendingAutoPlayEntry;
        WorldLoadWarningState.pendingAutoPlayEntry = null;
        client.setScreen(new DowngradeCompleteScreen(parent, worldName, entry));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
