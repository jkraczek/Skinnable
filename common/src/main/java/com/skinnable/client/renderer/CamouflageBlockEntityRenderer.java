package com.skinnable.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CamouflageBlockEntityRenderer<T extends BlockEntity & ICamouflageBlockEntity>
        implements BlockEntityRenderer<T> {

    public CamouflageBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(T entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        @Nullable BlockState camouflage = entity.getCamouflage();
        if (camouflage == null || camouflage.isAir()) return;
        Minecraft.getInstance().getBlockRenderer()
                .renderSingleBlock(camouflage, poseStack, buffer, packedLight, packedOverlay);
    }
}
