package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class WorldFormatChoiceScreen extends Screen {

    private final Screen parent;
    private final String levelName;
    private final Runnable onProceedWithoutDowngrade;

    public WorldFormatChoiceScreen(Screen parent, String levelName, Runnable onProceedWithoutDowngrade) {
        super(Text.literal("World Format Warning"));
        this.parent = parent;
        this.levelName = levelName;
        this.onProceedWithoutDowngrade = onProceedWithoutDowngrade;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int by = height / 2 + 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Downgrade world"), btn ->
            client.setScreen(new DowngradeSelectScreen(parent, levelName))
        ).dimensions(cx - 156, by, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Continue without Downgrading"), btn ->
            client.setScreen(new AlphaWarningScreen(this, levelName, confirmed -> {
                if (confirmed) onProceedWithoutDowngrade.run();
            }))
        ).dimensions(cx + 6, by, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn ->
            client.setScreen(parent)
        ).dimensions(cx - 50, by + 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 110;

        // Big title (1.5x)
        ctx.getMatrices().push();
        ctx.getMatrices().scale(1.5f, 1.5f, 1.0f);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("! World Format Warning !"),
            (int)(cx / 1.5f), (int)(y / 1.5f), 0xFF5555);
        ctx.getMatrices().pop();
        y += 28;

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("This world can be downgraded to the current version so you can play it without having the mod installed."), cx, y, 0xFFFFFF);
        y += 12;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("This is due to the underlying changes in the world format in 26.2-snapshot-1 versions. (Vulkan ones)"), cx, y, 0xFFFFFF);
        y += 12;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("However, you can play without downgrading by having this mod installed."), cx, y, 0xFFFFFF);
        y += 12;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("It will intercept Minecraft's file path lookups and NBT data reads to keep the world intact,"), cx, y, 0xFFFFFF);
        y += 12;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("but this requires constantly redirecting Minecraft's internal operations, which may go wrong."), cx, y, 0xFFFFFF);
        y += 16;

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("This world was created in a newer Minecraft version that uses a different folder structure and world format."), cx, y, 0xAAAAAA);
        y += 12;
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Downgrading reverts these changes, but Minecraft will reupgrade the world in versions 26.2-snapshot-1 and above."), cx, y, 0xAAAAAA);
        y += 20;

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Downgrading most likely can cause data loss if you had blocks, items or entities not in this version"), cx, y, 0xFF5555);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
