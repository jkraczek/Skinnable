package com.skinnable.client.screen;

import com.skinnable.data.SpawnEntry;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SkinnableSpawnerScreen extends Screen {

    private final BlockPos blockPos;

    // Settings fields
    private EditBox minDelayBox;
    private EditBox maxDelayBox;
    private EditBox spawnCountBox;
    private EditBox maxNearbyBox;
    private EditBox playerRangeBox;
    private EditBox searchBox;

    // Data
    private final List<SelectedEntry> selectedEntries = new ArrayList<>();

    // Widgets
    private EntityListWidget entityListWidget;
    private SelectedEntriesWidget selectedEntriesWidget;

    public SkinnableSpawnerScreen(BlockPos blockPos) {
        super(Component.translatable("screen.skinnable.spawner_config"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        int leftX = 5;
        int midX = 165;
        int rightX = 335;
        int topY = 25;

        // Search box
        searchBox = new EditBox(font, leftX, topY, 155, 20,
                Component.translatable("screen.skinnable.search_entities"));
        searchBox.setHint(Component.translatable("screen.skinnable.search_entities"));
        searchBox.setResponder(text -> {
            if (entityListWidget != null) entityListWidget.updateFilter(text);
        });
        addRenderableWidget(searchBox);

        // Entity list
        entityListWidget = new EntityListWidget(minecraft, 155, height - topY - 55, topY + 25, leftX);
        addWidget(entityListWidget);

        // Selected entries widget
        selectedEntriesWidget = new SelectedEntriesWidget(minecraft, 165, height - topY - 55, topY + 5, midX);
        addWidget(selectedEntriesWidget);

        // Settings
        int settingY = topY;
        int settingBoxW = 60;
        int settingBoxX = rightX + 130 - 30;

        minDelayBox = new EditBox(font, settingBoxX, settingY, settingBoxW, 20,
                Component.translatable("screen.skinnable.min_delay"));
        minDelayBox.setValue("200");
        addRenderableWidget(minDelayBox);
        settingY += 25;

        maxDelayBox = new EditBox(font, settingBoxX, settingY, settingBoxW, 20,
                Component.translatable("screen.skinnable.max_delay"));
        maxDelayBox.setValue("800");
        addRenderableWidget(maxDelayBox);
        settingY += 25;

        spawnCountBox = new EditBox(font, settingBoxX, settingY, settingBoxW, 20,
                Component.translatable("screen.skinnable.spawn_count"));
        spawnCountBox.setValue("4");
        addRenderableWidget(spawnCountBox);
        settingY += 25;

        maxNearbyBox = new EditBox(font, settingBoxX, settingY, settingBoxW, 20,
                Component.translatable("screen.skinnable.max_nearby"));
        maxNearbyBox.setValue("6");
        addRenderableWidget(maxNearbyBox);
        settingY += 25;

        playerRangeBox = new EditBox(font, settingBoxX, settingY, settingBoxW, 20,
                Component.translatable("screen.skinnable.player_range"));
        playerRangeBox.setValue("16");
        addRenderableWidget(playerRangeBox);

        // Save button
        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.save"),
                btn -> saveAndClose()
        ).bounds(width / 2 - 105, height - 30, 100, 20).build());

        // Cancel button
        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.cancel"),
                btn -> onClose()
        ).bounds(width / 2 + 5, height - 30, 100, 20).build());
    }

    private void saveAndClose() {
        List<SpawnEntry> entries = new ArrayList<>();
        for (SelectedEntry se : selectedEntries) {
            int w = 1;
            try { w = Math.max(1, Math.min(100, Integer.parseInt(se.weightBox.getValue()))); } catch (NumberFormatException ignored) {}
            entries.add(new SpawnEntry(se.entityTypeId, w));
        }
        int minDelay = parseIntClamp(minDelayBox.getValue(), 10, 0, 1200);
        int maxDelay = parseIntClamp(maxDelayBox.getValue(), 40, minDelay, 1200);
        int spawnCount = parseIntClamp(spawnCountBox.getValue(), 4, 1, 16);
        int maxNearby = parseIntClamp(maxNearbyBox.getValue(), 6, 1, 20);
        int playerRange = parseIntClamp(playerRangeBox.getValue(), 16, 0, 64);

        C2SUpdateSpawnerPacket packet = new C2SUpdateSpawnerPacket(
                blockPos, entries, minDelay, maxDelay, spawnCount, maxNearby, playerRange
        );
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(packet));
        }
        onClose();
    }

    private static int parseIntClamp(String s, int def, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(s)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        extractBackground(extractor, mouseX, mouseY, partialTick);
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        extractor.text(font, title, width / 2 - font.width(title) / 2, 8, 0xFFFFFF);
        extractor.text(font, "Available Entities:", 5, 17, 0xAAAAAA);
        extractor.text(font, Component.translatable("screen.skinnable.selected_mobs"), 165, 17, 0xAAAAAA);
        extractor.text(font, Component.translatable("screen.skinnable.spawn_settings"), 335, 17, 0xAAAAAA);

        int rightX = 335;
        int settingY = 25;
        int step = 25;
        String[] labels = {
            "Min Delay:", "Max Delay:", "Spawn Count:", "Max Nearby:", "Player Range:"
        };
        for (String label : labels) {
            extractor.text(font, label, rightX, settingY + 5, 0xFFFFFF);
            settingY += step;
        }

        entityListWidget.extractRenderState(extractor, mouseX, mouseY, partialTick);
        selectedEntriesWidget.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (entityListWidget.mouseClicked(event, focused)) return true;
        if (selectedEntriesWidget.mouseClicked(event, focused)) return true;
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (entityListWidget.isMouseOver(mouseX, mouseY)) {
            return entityListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (selectedEntriesWidget.isMouseOver(mouseX, mouseY)) {
            return selectedEntriesWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void addToSelected(Identifier entityTypeId, String displayName) {
        boolean alreadyAdded = selectedEntries.stream().anyMatch(se -> se.entityTypeId.equals(entityTypeId));
        if (!alreadyAdded) {
            selectedEntries.add(new SelectedEntry(entityTypeId, displayName, this));
            selectedEntriesWidget.refreshEntries();
        }
    }

    private void removeFromSelected(SelectedEntry entry) {
        selectedEntries.remove(entry);
        selectedEntriesWidget.refreshEntries();
    }

    // Entity List Widget
    class EntityListWidget extends AbstractSelectionList<EntityListWidget.EntityEntry> {
        private final List<EntityType<?>> allEntities;
        private String currentFilter = "";

        EntityListWidget(Minecraft mc, int width, int height, int y, int x) {
            super(mc, width, height, y, 18);
            this.setX(x);
            this.allEntities = BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(et -> et != EntityType.PLAYER)
                    .sorted(Comparator.comparing(et -> BuiltInRegistries.ENTITY_TYPE.getKey(et).toString()))
                    .collect(Collectors.toList());
            refreshEntries(currentFilter);
        }

        void updateFilter(String filter) {
            currentFilter = filter.toLowerCase();
            refreshEntries(currentFilter);
        }

        private void refreshEntries(String filter) {
            clearEntries();
            for (EntityType<?> et : allEntities) {
                Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(et);
                if (key == null) continue;
                String name = key.toString();
                if (filter.isEmpty() || name.contains(filter)) {
                    addEntry(new EntityEntry(et, key));
                }
            }
        }

        @Override
        public int getRowWidth() { return width - 6; }

        @Override
        protected int scrollBarX() { return getX() + width - 6; }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        class EntityEntry extends AbstractSelectionList.Entry<EntityEntry> {
            private final EntityType<?> entityType;
            private final Identifier key;
            private final String displayName;

            EntityEntry(EntityType<?> entityType, Identifier key) {
                this.entityType = entityType;
                this.key = key;
                this.displayName = key.getPath().replace('_', ' ');
            }

            @Override
            public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY,
                                       boolean focused, float partialTick) {
                if (isMouseOver(mouseX, mouseY)) {
                    extractor.fill(getContentX(), getContentY(), getContentRight(), getContentBottom(), 0x44FFFFFF);
                }
                extractor.text(SkinnableSpawnerScreen.this.font, displayName, getContentX() + 3, getContentY() + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
                if (event.button() == 0) {
                    SkinnableSpawnerScreen.this.addToSelected(key, displayName);
                    return true;
                }
                return false;
            }

            public Component getNarration() { return Component.literal(displayName); }
        }
    }

    // Selected Entries Widget
    class SelectedEntriesWidget extends AbstractSelectionList<SelectedEntriesWidget.Row> {
        SelectedEntriesWidget(Minecraft mc, int width, int height, int y, int x) {
            super(mc, width, height, y, 22);
            this.setX(x);
            refreshEntries();
        }

        void refreshEntries() {
            clearEntries();
            for (SelectedEntry se : selectedEntries) {
                addEntry(new Row(se));
            }
        }

        @Override
        public int getRowWidth() { return width - 6; }

        @Override
        protected int scrollBarX() { return getX() + width - 6; }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        class Row extends AbstractSelectionList.Entry<Row> {
            private final SelectedEntry entry;
            private final Button removeBtn;

            Row(SelectedEntry entry) {
                this.entry = entry;
                this.removeBtn = Button.builder(Component.literal("X"), btn -> {
                    SkinnableSpawnerScreen.this.removeFromSelected(entry);
                }).bounds(0, 0, 18, 18).build();
            }

            @Override
            public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY,
                                       boolean focused, float partialTick) {
                int left = getContentX();
                int top = getContentY();
                int w = getContentWidth();

                extractor.text(SkinnableSpawnerScreen.this.font, entry.displayName, left + 3, top + 5, 0xFFFFFF);
                extractor.text(SkinnableSpawnerScreen.this.font, "W:", left + w - 80, top + 5, 0xAAAAAA);

                entry.weightBox.setX(left + w - 65);
                entry.weightBox.setY(top + 2);
                entry.weightBox.extractRenderState(extractor, mouseX, mouseY, partialTick);

                removeBtn.setX(left + w - 22);
                removeBtn.setY(top + 2);
                removeBtn.extractRenderState(extractor, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
                if (removeBtn.mouseClicked(event, focused)) return true;
                if (entry.weightBox.mouseClicked(event, focused)) return true;
                return false;
            }

            @Override
            public boolean keyPressed(KeyEvent event) {
                return entry.weightBox.keyPressed(event);
            }

            @Override
            public boolean charTyped(CharacterEvent event) {
                return entry.weightBox.charTyped(event);
            }

            public Component getNarration() { return Component.literal(entry.displayName); }
        }
    }

    // Selected Entry data holder
    static class SelectedEntry {
        final Identifier entityTypeId;
        final String displayName;
        final EditBox weightBox;

        SelectedEntry(Identifier entityTypeId, String displayName, Screen screen) {
            this.entityTypeId = entityTypeId;
            this.displayName = displayName;
            this.weightBox = new EditBox(Minecraft.getInstance().font, 0, 0, 40, 18,
                    Component.literal("1"));
            weightBox.setValue("1");
            weightBox.setMaxLength(3);
        }
    }
}
