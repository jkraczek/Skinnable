package com.skinnable.client.screen;

import com.skinnable.data.SpawnEntry;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
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

    // Entity list (left column)
    private List<EntityInfo> allEntities = new ArrayList<>();
    private List<EntityInfo> filteredEntities = new ArrayList<>();
    private int entityScroll = 0;
    private static final int ENTITY_ROW_H = 18;

    // Selected entries (middle column)
    private final List<SelectedEntry> selectedEntries = new ArrayList<>();
    private int selectedScroll = 0;
    private static final int SELECTED_ROW_H = 22;

    // Single shared EditBox for weight editing
    private EditBox weightEditBox;
    private int editingWeightIdx = -1;

    // Layout — computed in init
    private int listTopY;
    private int listHeight;
    private static final int LEFT_X = 5;
    private static final int LEFT_W = 155;
    private static final int MID_X = 165;
    private int midW;            // computed from screen width so it never overlaps settings
    private int settingsLabelX;  // left edge of the settings column

    // Initial settings values (passed in constructor, applied in init)
    private final int initMinDelay, initMaxDelay, initSpawnCount, initMaxNearby, initPlayerRange;
    private final List<SpawnEntry> initEntries;

    // Settings widgets
    private EditBox minDelayBox, maxDelayBox, spawnCountBox, maxNearbyBox, playerRangeBox;
    private EditBox searchBox;

    public SkinnableSpawnerScreen(BlockPos blockPos, List<SpawnEntry> currentEntries,
                                  int minDelay, int maxDelay, int spawnCount,
                                  int maxNearby, int playerRange) {
        super(Component.translatable("screen.skinnable.spawner_config"));
        this.blockPos = blockPos;
        this.initEntries = currentEntries;
        this.initMinDelay = minDelay;
        this.initMaxDelay = maxDelay;
        this.initSpawnCount = spawnCount;
        this.initMaxNearby = maxNearby;
        this.initPlayerRange = playerRange;
    }

    // Convenience constructor for new (empty) spawner
    public SkinnableSpawnerScreen(BlockPos blockPos) {
        this(blockPos, List.of(), 200, 800, 4, 6, 16);
    }

    @Override
    protected void init() {
        listTopY = 50;
        listHeight = height - listTopY - 40;
        // Settings column: labels from (width-195), boxes from (width-130).
        // Middle column fills the gap between left and settings, with a 5px margin.
        settingsLabelX = width - 195;
        midW = Math.max(80, settingsLabelX - MID_X - 5);

        // Build entity type list once
        if (allEntities.isEmpty()) {
            BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(et -> et != EntityType.PLAYER && BuiltInRegistries.ENTITY_TYPE.getKey(et) != null)
                    .sorted(Comparator.comparing(et -> BuiltInRegistries.ENTITY_TYPE.getKey(et).toString()))
                    .forEach(et -> {
                        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(et);
                        allEntities.add(new EntityInfo(key, key.getPath().replace('_', ' ')));
                    });
            filteredEntities = new ArrayList<>(allEntities);
        }

        // Populate selected entries from initial data (only on first init)
        if (selectedEntries.isEmpty() && !initEntries.isEmpty()) {
            for (SpawnEntry se : initEntries) {
                String displayName = se.entityType().getPath().replace('_', ' ');
                selectedEntries.add(new SelectedEntry(se.entityType(), displayName, se.weight()));
            }
        }

        // Search box
        searchBox = new EditBox(font, LEFT_X, listTopY - 25, LEFT_W, 20, Component.empty());
        searchBox.setHint(Component.literal("Search..."));
        searchBox.setResponder(text -> {
            String lower = text.toLowerCase();
            filteredEntities = allEntities.stream()
                    .filter(e -> lower.isEmpty() || e.id.toString().contains(lower) || e.displayName.contains(lower))
                    .collect(Collectors.toList());
            entityScroll = 0;
        });
        addRenderableWidget(searchBox);

        // Settings boxes (right column) — labels at settingsLabelX, boxes 65px to the right
        int settingsBoxX = settingsLabelX + 65;
        int settY = listTopY;
        minDelayBox    = makeSettingBox(settingsBoxX, settY, String.valueOf(initMinDelay));    settY += 25;
        maxDelayBox    = makeSettingBox(settingsBoxX, settY, String.valueOf(initMaxDelay));    settY += 25;
        spawnCountBox  = makeSettingBox(settingsBoxX, settY, String.valueOf(initSpawnCount));  settY += 25;
        maxNearbyBox   = makeSettingBox(settingsBoxX, settY, String.valueOf(initMaxNearby));   settY += 25;
        playerRangeBox = makeSettingBox(settingsBoxX, settY, String.valueOf(initPlayerRange));

        // Weight edit box — hidden off-screen until user clicks a weight cell
        weightEditBox = new EditBox(font, -2000, -2000, 38, 18, Component.empty());
        weightEditBox.setMaxLength(3);
        addRenderableWidget(weightEditBox);

        // Save / Cancel
        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.save"), btn -> saveAndClose()
        ).bounds(width / 2 - 105, height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.cancel"), btn -> onClose()
        ).bounds(width / 2 + 5, height - 30, 100, 20).build());
    }

    private EditBox makeSettingBox(int x, int y, String value) {
        EditBox box = new EditBox(font, x, y, 60, 20, Component.empty());
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        // Title
        extractor.text(font, title, width / 2 - font.width(title) / 2, 8, 0xFFFFFFFF);

        // Column headers
        extractor.text(font, "Available Mobs:", LEFT_X, listTopY - 35, 0xFFAAAAAA);
        extractor.text(font, "Selected Mobs:", MID_X, listTopY - 12, 0xFFAAAAAA);
        extractor.text(font, "Spawn Settings:", settingsLabelX, listTopY - 12, 0xFFAAAAAA);

        // Settings labels (left-aligned within settings column)
        int settY = listTopY;
        String[] labels = {"Min Delay:", "Max Delay:", "Spawn Count:", "Max Nearby:", "Player Range:"};
        for (String label : labels) {
            extractor.text(font, label, settingsLabelX + 2, settY + 5, 0xFFFFFFFF);
            settY += 25;
        }

        // ── Entity list (left) ─────────────────────────────────────────────
        int listBottom = listTopY + listHeight;
        extractor.fill(LEFT_X, listTopY, LEFT_X + LEFT_W, listBottom, 0xAA000000);
        for (int i = entityScroll; i < filteredEntities.size(); i++) {
            int ey = listTopY + (i - entityScroll) * ENTITY_ROW_H;
            if (ey + ENTITY_ROW_H > listBottom) break;
            if (mouseX >= LEFT_X && mouseX < LEFT_X + LEFT_W && mouseY >= ey && mouseY < ey + ENTITY_ROW_H) {
                extractor.fill(LEFT_X, ey, LEFT_X + LEFT_W, ey + ENTITY_ROW_H, 0x55FFFFFF);
            }
            extractor.text(font, filteredEntities.get(i).displayName, LEFT_X + 3, ey + 4, 0xFFFFFFFF);
        }

        // ── Selected entries (middle) ──────────────────────────────────────
        int midRight = MID_X + midW;
        extractor.fill(MID_X, listTopY, midRight, listBottom, 0xAA000000);

        // Update weight box position if visible entry is being edited
        if (editingWeightIdx >= 0) {
            int visIdx = editingWeightIdx - selectedScroll;
            if (visIdx >= 0) {
                int sy = listTopY + visIdx * SELECTED_ROW_H;
                if (sy + SELECTED_ROW_H <= listBottom) {
                    weightEditBox.setX(midRight - 60);
                    weightEditBox.setY(sy + 2);
                } else {
                    weightEditBox.setX(-2000); // scrolled out of view
                }
            } else {
                weightEditBox.setX(-2000);
            }
        }

        for (int i = selectedScroll; i < selectedEntries.size(); i++) {
            int sy = listTopY + (i - selectedScroll) * SELECTED_ROW_H;
            if (sy + SELECTED_ROW_H > listBottom) break;
            SelectedEntry se = selectedEntries.get(i);

            extractor.text(font, se.displayName, MID_X + 3, sy + 6, 0xFFFFFFFF);

            // Weight: show EditBox when editing, plain text otherwise
            if (editingWeightIdx != i) {
                extractor.text(font, "W:" + se.weight, midRight - 62, sy + 6, 0xFFAAAAAA);
            }

            // X remove button
            boolean xHover = mouseX >= midRight - 22 && mouseX < midRight - 2
                    && mouseY >= sy + 2 && mouseY < sy + SELECTED_ROW_H - 2;
            extractor.fill(midRight - 22, sy + 2, midRight - 2, sy + SELECTED_ROW_H - 2,
                    xHover ? 0xFFAA3333 : 0xFF662222);
            extractor.text(font, "X", midRight - 17, sy + 6, 0xFFFFAAAA);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        int mx = (int) event.x();
        int my = (int) event.y();
        int listBottom = listTopY + listHeight;

        if (event.button() == 0) {
            // Entity list click → add mob
            if (mx >= LEFT_X && mx < LEFT_X + LEFT_W && my >= listTopY && my < listBottom) {
                int row = entityScroll + (my - listTopY) / ENTITY_ROW_H;
                if (row >= 0 && row < filteredEntities.size()) {
                    saveCurrentWeightEdit();
                    EntityInfo info = filteredEntities.get(row);
                    if (selectedEntries.stream().noneMatch(se -> se.id.equals(info.id))) {
                        selectedEntries.add(new SelectedEntry(info.id, info.displayName, 1));
                    }
                }
                return true;
            }

            int midRight = MID_X + midW;

            // Selected entries area clicks
            if (mx >= MID_X && mx < midRight && my >= listTopY && my < listBottom) {
                int visIdx = (my - listTopY) / SELECTED_ROW_H;
                int actualIdx = selectedScroll + visIdx;
                if (actualIdx >= 0 && actualIdx < selectedEntries.size()) {
                    int sy = listTopY + visIdx * SELECTED_ROW_H;

                    // X button
                    if (mx >= midRight - 22 && mx < midRight - 2 && my >= sy + 2 && my < sy + SELECTED_ROW_H - 2) {
                        saveCurrentWeightEdit();
                        selectedEntries.remove(actualIdx);
                        if (editingWeightIdx >= selectedEntries.size()) {
                            editingWeightIdx = -1;
                            weightEditBox.setX(-2000);
                        }
                        return true;
                    }

                    // Weight cell click → start editing
                    if (mx >= midRight - 62 && mx < midRight - 22) {
                        if (editingWeightIdx != actualIdx) {
                            saveCurrentWeightEdit();
                            editingWeightIdx = actualIdx;
                            weightEditBox.setX(midRight - 60);
                            weightEditBox.setY(sy + 2);
                            weightEditBox.setValue(String.valueOf(selectedEntries.get(actualIdx).weight));
                        }
                        // fall through to super so the EditBox can receive the click
                    }
                }
            }
        }

        boolean result = super.mouseClicked(event, focused);

        // If click landed outside the active weight box, save the edit
        if (event.button() == 0 && editingWeightIdx >= 0) {
            int visIdx = editingWeightIdx - selectedScroll;
            int sy = listTopY + visIdx * SELECTED_ROW_H;
            int midRight = MID_X + midW;
            boolean onWeightBox = mx >= midRight - 62 && mx < midRight - 22
                    && my >= sy + 2 && my < sy + SELECTED_ROW_H - 2;
            if (!onWeightBox) {
                saveCurrentWeightEdit();
            }
        }

        return result;
    }

    private void saveCurrentWeightEdit() {
        if (editingWeightIdx >= 0 && editingWeightIdx < selectedEntries.size()) {
            try {
                int w = Math.max(1, Math.min(100, Integer.parseInt(weightEditBox.getValue())));
                selectedEntries.get(editingWeightIdx).weight = w;
            } catch (NumberFormatException ignored) {}
        }
        editingWeightIdx = -1;
        weightEditBox.setX(-2000);
        weightEditBox.setY(-2000);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listBottom = listTopY + listHeight;
        int dy = (int) -Math.signum(scrollY);

        if (mouseX >= LEFT_X && mouseX < LEFT_X + LEFT_W && mouseY >= listTopY && mouseY < listBottom) {
            int maxScroll = Math.max(0, filteredEntities.size() - listHeight / ENTITY_ROW_H);
            entityScroll = Math.max(0, Math.min(entityScroll + dy, maxScroll));
            return true;
        }
        if (mouseX >= MID_X && mouseX < MID_X + midW && mouseY >= listTopY && mouseY < listBottom) {
            saveCurrentWeightEdit();
            int maxScroll = Math.max(0, selectedEntries.size() - listHeight / SELECTED_ROW_H);
            selectedScroll = Math.max(0, Math.min(selectedScroll + dy, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        saveCurrentWeightEdit();
        super.onClose();
    }

    private void saveAndClose() {
        saveCurrentWeightEdit();
        List<SpawnEntry> entries = new ArrayList<>();
        for (SelectedEntry se : selectedEntries) {
            entries.add(new SpawnEntry(se.id, se.weight));
        }
        int minDelay = parseClamp(minDelayBox.getValue(), 200, 0, 1200);
        int maxDelay = parseClamp(maxDelayBox.getValue(), 800, minDelay, 1200);
        int spawnCount = parseClamp(spawnCountBox.getValue(), 4, 1, 16);
        int maxNearby = parseClamp(maxNearbyBox.getValue(), 6, 1, 20);
        int playerRange = parseClamp(playerRangeBox.getValue(), 16, 0, 64);

        C2SUpdateSpawnerPacket packet = new C2SUpdateSpawnerPacket(
                blockPos, entries, minDelay, maxDelay, spawnCount, maxNearby, playerRange);
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(packet));
        }
        onClose();
    }

    private static int parseClamp(String s, int def, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(s))); }
        catch (NumberFormatException e) { return def; }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Data classes ──────────────────────────────────────────────────────

    static class EntityInfo {
        final Identifier id;
        final String displayName;
        EntityInfo(Identifier id, String displayName) { this.id = id; this.displayName = displayName; }
    }

    static class SelectedEntry {
        final Identifier id;
        final String displayName;
        int weight;
        SelectedEntry(Identifier id, String displayName, int weight) {
            this.id = id; this.displayName = displayName; this.weight = weight;
        }
    }
}
