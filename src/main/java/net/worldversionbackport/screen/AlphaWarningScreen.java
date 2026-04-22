package net.worldversionbackport.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class AlphaWarningScreen extends Screen {

    private final Screen parent;
    private final String levelName;
    private final Consumer<Boolean> callback;

    public AlphaWarningScreen(Screen parent, Consumer<Boolean> callback) {
        this(parent, null, callback);
    }

    public AlphaWarningScreen(Screen parent, String levelName, Consumer<Boolean> callback) {
        super(Text.literal("WorldVersionBackport - WARNING"));
        this.parent = parent;
        this.levelName = levelName;
        this.callback = callback;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int by = height / 2 + 65;

        if (levelName != null) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Create backup and load"), btn ->
                client.setScreen(new BackupConfirmScreen(this, levelName, () -> callback.accept(true)))
            ).dimensions(cx - 156, by, 150, 20).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("I know what im doing!"), btn ->
            callback.accept(true)
        ).dimensions(cx + 6, by, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> {
            callback.accept(false);
            client.setScreen(parent);
        }).dimensions(cx - 50, by + 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 110;

        // "! WARNING !" at 3x scale (50% bigger than before)
        context.getMatrices().push();
        context.getMatrices().scale(3.0f, 3.0f, 1.0f);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("! WARNING !"), cx / 3, y / 3, 0xFF5555);
        context.getMatrices().pop();
        y += 40;

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("This mod is in ALPHA."), cx, y, 0xFF5555);
        y += 12;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("I am NOT responsible for YOUR CARELESSNESS in not having world backups if something goes wrong"), cx, y, 0xFFFFFF);
        y += 12;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("and your world gets corrupted. (eg. files ending up in the wrong place due to a missed intercept)"), cx, y, 0xFFFFFF);
        y += 12;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Backups are IMPORTANT during this stage of the mod."), cx, y, 0xFFFF55);
        y += 20;

        // Item deletion warning
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("ANY ITEMS IN CONTAINERS OR INVENTORIES NOT IN THIS VERSION"), cx, y, 0xFF5555);
        y += 11;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("WILL BE DELETED. THIS IS WHAT JAVA DOES,"), cx, y, 0xFF5555);
        y += 11;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("UNLESS A MOD EXISTS THAT CAN OVERRIDE THIS."), cx, y, 0xFF5555);
        y += 16;

        // "This warning will show every time..." rendered below Cancel button
        int cancelBottom = height / 2 + 65 + 28 + 20 + 6; // by + 28 + buttonH + gap
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("This warning will show every time during alpha and beta stages."),
            cx, cancelBottom, 0x888888);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
