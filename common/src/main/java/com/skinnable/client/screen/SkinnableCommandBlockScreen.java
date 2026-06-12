package com.skinnable.client.screen;

import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.network.packet.C2SSaveCommandBlockPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class SkinnableCommandBlockScreen extends Screen {

    private final BlockPos pos;
    private String currentMode;

    private EditBox commandBox;
    private Button modeRedstoneBtn;
    private Button modeAutoBtn;
    private Button modeSequenceBtn;

    public SkinnableCommandBlockScreen(BlockPos pos, String command, String mode) {
        super(Component.translatable("screen.skinnable.command_block_config"));
        this.pos = pos;
        this.currentMode = mode;
        // Store command to restore after init
        this._initialCommand = command;
    }

    // Temporary storage for command before init() sets up the EditBox
    private final String _initialCommand;

    @Override
    protected void init() {
        int centerX = width / 2;
        int topY = 30;

        // Wide command input box
        commandBox = new EditBox(font, centerX - 150, topY, 300, 20,
                Component.translatable("screen.skinnable.command"));
        commandBox.setHint(Component.literal("/say Hello World"));
        commandBox.setValue(_initialCommand);
        commandBox.setMaxLength(32500);
        addRenderableWidget(commandBox);

        // Mode buttons
        int btnY = topY + 30;
        modeRedstoneBtn = Button.builder(
                Component.translatable("screen.skinnable.mode_redstone"),
                btn -> { currentMode = SkinnableCommandBlockEntity.Mode.REDSTONE.name(); updateModeButtons(); }
        ).bounds(centerX - 150, btnY, 90, 20).build();
        addRenderableWidget(modeRedstoneBtn);

        modeAutoBtn = Button.builder(
                Component.translatable("screen.skinnable.mode_auto"),
                btn -> { currentMode = SkinnableCommandBlockEntity.Mode.AUTO.name(); updateModeButtons(); }
        ).bounds(centerX - 50, btnY, 90, 20).build();
        addRenderableWidget(modeAutoBtn);

        modeSequenceBtn = Button.builder(
                Component.translatable("screen.skinnable.mode_sequence"),
                btn -> { currentMode = SkinnableCommandBlockEntity.Mode.SEQUENCE.name(); updateModeButtons(); }
        ).bounds(centerX + 60, btnY, 90, 20).build();
        addRenderableWidget(modeSequenceBtn);

        updateModeButtons();

        // Save and Cancel
        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.save"),
                btn -> saveAndClose()
        ).bounds(centerX - 105, height - 30, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.cancel"),
                btn -> onClose()
        ).bounds(centerX + 5, height - 30, 100, 20).build());
    }

    private void updateModeButtons() {
        if (modeRedstoneBtn != null) modeRedstoneBtn.active = !currentMode.equals(SkinnableCommandBlockEntity.Mode.REDSTONE.name());
        if (modeAutoBtn != null)     modeAutoBtn.active     = !currentMode.equals(SkinnableCommandBlockEntity.Mode.AUTO.name());
        if (modeSequenceBtn != null) modeSequenceBtn.active = !currentMode.equals(SkinnableCommandBlockEntity.Mode.SEQUENCE.name());
    }

    private void saveAndClose() {
        String command = commandBox != null ? commandBox.getValue() : "";
        C2SSaveCommandBlockPacket packet = new C2SSaveCommandBlockPacket(pos, command, currentMode);
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(packet));
        }
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
        extractor.text(font, title, width / 2 - font.width(title) / 2, 10, 0xFFFFFFFF);
        extractor.text(font, Component.translatable("screen.skinnable.command"), width / 2 - 150, 22, 0xFFAAAAAA);
        extractor.text(font, Component.translatable("screen.skinnable.mode"), width / 2 - 150, 55, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
