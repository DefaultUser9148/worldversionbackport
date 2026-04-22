package net.worldversionbackport.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WvbWorldListWidget extends AlwaysSelectedEntryListWidget<WvbWorldListWidget.WorldEntry> {

    private final Path savesDir;
    private final Predicate<Path> filter;
    private final Consumer<WorldEntry> onSelect;

    public WvbWorldListWidget(MinecraftClient client, int width, int height, int top, int itemHeight,
                               Path savesDir, Predicate<Path> filter, Consumer<WorldEntry> onSelect) {
        super(client, width, height, top, itemHeight);
        this.savesDir = savesDir;
        this.filter = filter;
        this.onSelect = onSelect;
        refresh();
    }

    public void refresh() {
        clearEntries();
        try (Stream<Path> paths = Files.list(savesDir)) {
            paths.filter(Files::isDirectory)
                 .filter(filter)
                 .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                 .forEach(p -> addEntry(new WorldEntry(p.getFileName().toString())));
        } catch (IOException ignored) {}
    }

    public int size() {
        return children().size();
    }

    public void preselectByName(String name) {
        for (WorldEntry e : children()) {
            if (e.name.equals(name)) {
                setSelected(e);
                centerScrollOn(e);
                return;
            }
        }
    }

    public class WorldEntry extends AlwaysSelectedEntryListWidget.Entry<WorldEntry> {
        public final String name;

        WorldEntry(String name) {
            this.name = name;
        }

        @Override
        public Text getNarration() {
            return Text.literal(name);
        }

        @Override
        public void render(DrawContext ctx, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean selected = WvbWorldListWidget.this.getSelectedOrNull() == this;
            if (selected) {
                ctx.fill(x, y, x + entryWidth, y + entryHeight, 0xFF4444AA);
            } else if (hovered) {
                ctx.fill(x, y, x + entryWidth, y + entryHeight, 0xFF333333);
            }
            ctx.drawTextWithShadow(WvbWorldListWidget.this.client.textRenderer,
                Text.literal(name), x + 4, y + (entryHeight - 8) / 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            WvbWorldListWidget.this.setSelected(this);
            if (onSelect != null) onSelect.accept(this);
            return true;
        }
    }
}
