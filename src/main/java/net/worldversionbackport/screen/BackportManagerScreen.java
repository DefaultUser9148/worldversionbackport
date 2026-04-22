package net.worldversionbackport.screen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.worldversionbackport.WorldVersionBackport;
import net.worldversionbackport.widget.WvbLinkButton;

public class BackportManagerScreen extends Screen {

    private static final Identifier GITHUB_ICON   = Identifier.of(WorldVersionBackport.MOD_ID, "textures/gui/github.png");
    private static final Identifier MODRINTH_ICON = Identifier.of(WorldVersionBackport.MOD_ID, "textures/gui/modrinth.png");
    private static final Identifier FABRIC_ICON   = Identifier.of(WorldVersionBackport.MOD_ID, "textures/gui/fabric.png");

    private static final String GITHUB_URL   = "https://github.com/DefaultUser9148/worldversionbackport";
    private static final String MODRINTH_URL = "https://modrinth.com/project/worldversionbackport/";
    private static final String FABRIC_URL   = "https://fabricmc.net/";

    private final Screen parent;

    public BackportManagerScreen(Screen parent) {
        super(Text.literal("WorldVersionBackport"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 22;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Downgrade a World..."),
            btn -> client.setScreen(new DowngradeSelectScreen(this))
        ).dimensions(cx - 100, y, 200, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Extra Options..."),
            btn -> client.setScreen(new WvbOptionsScreen(this))
        ).dimensions(cx - 100, y, 200, 20).build());
        y += 28;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Back"),
            btn -> client.setScreen(parent)
        ).dimensions(cx - 50, y, 100, 20).build());
        y += 30;

        int iconSize = 20;
        int totalW = iconSize * 3 + 4 * 2;
        int linkRowX = cx - totalW / 2;
        addDrawableChild(new WvbLinkButton(linkRowX,                    y, iconSize, GITHUB_ICON,   Text.literal("GitHub"),   GITHUB_URL));
        addDrawableChild(new WvbLinkButton(linkRowX + iconSize + 4,     y, iconSize, MODRINTH_ICON, Text.literal("Modrinth"), MODRINTH_URL));
        addDrawableChild(new WvbLinkButton(linkRowX + (iconSize + 4)*2, y, iconSize, FABRIC_ICON,   Text.literal("Fabric"),   FABRIC_URL));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = height / 2 - 90;

        context.drawCenteredTextWithShadow(textRenderer, title, cx, y, 0xFFFFFF);
        y += 11;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("v" + WorldVersionBackport.MOD_VERSION + "  \u00b7  ALPHA"),
            cx, y, 0xAAAAAA);
        y += 11;

        String mcVer = SharedConstants.getGameVersion().getName();
        String fabricVer = FabricLoader.getInstance()
            .getModContainer("fabricloader")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("?");
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("MC " + mcVer + "  \u00b7  Fabric " + fabricVer),
            cx, y, 0x888888);
        y += 20;

        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Servers: NOT supported, UNKNOWN if server support is possible"),
            cx, y, 0xFF5555);
        y += 11;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("or if Minecraft handles it automatically."),
            cx, y, 0xFF5555);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
