package com.skinnable.client.screen;

import com.skinnable.network.packet.C2SUpdateTNTPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class SkinnableTNTScreen extends Screen {

    private final BlockPos pos;
    private final int initExplosionPower;

    private EditBox powerBox;

    public SkinnableTNTScreen(BlockPos pos, int explosionPower) {
        super(Component.translatable("screen.skinnable.tnt_config"));
        this.pos = pos;
        this.initExplosionPower = explosionPower;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        powerBox = new EditBox(font, centerX - 30, centerY - 10, 60, 20,
                Component.empty());
        powerBox.setMaxLength(3);
        powerBox.setValue(String.valueOf(initExplosionPower));
        addRenderableWidget(powerBox);

        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.save"), btn -> saveAndClose()
        ).bounds(centerX - 105, height - 30, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("screen.skinnable.cancel"), btn -> onClose()
        ).bounds(centerX + 5, height - 30, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int centerY = height / 2;

        extractor.text(font, title, centerX - font.width(title) / 2, 15, 0xFFFFFFFF);

        String label = "Explosion Strength:";
        extractor.text(font, label, centerX - font.width(label) / 2, centerY - 25, 0xFFFFFFFF);

        String hint = "(1 - 100, vanilla TNT = 4)";
        extractor.text(font, hint, centerX - font.width(hint) / 2, centerY + 15, 0xFFAAAAAA);
    }

    private void saveAndClose() {
        int power;
        try {
            power = Math.max(1, Math.min(100, Integer.parseInt(powerBox.getValue())));
        } catch (NumberFormatException e) {
            power = 4;
        }
        C2SUpdateTNTPacket packet = new C2SUpdateTNTPacket(pos, power);
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(packet));
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
