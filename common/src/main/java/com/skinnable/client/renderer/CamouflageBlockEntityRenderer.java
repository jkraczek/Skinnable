package com.skinnable.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CamouflageBlockEntityRenderer<T extends BlockEntity & ICamouflageBlockEntity>
        implements BlockEntityRenderer<T, CamouflageBlockEntityRenderer.RenderState> {

    public CamouflageBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class RenderState extends BlockEntityRenderState {
        @Nullable public MovingBlockRenderState camo;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(T entity, RenderState state, float partialTick, Vec3 camera,
                                    ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, overlay);
        state.camo = null;
        BlockState camouflage = entity.getCamouflage();
        if (camouflage == null || camouflage.isAir()) return;
        Level level = entity.getLevel();
        if (!(level instanceof ClientLevel clientLevel)) return;
        MovingBlockRenderState movingState = new MovingBlockRenderState();
        movingState.randomSeedPos = entity.getBlockPos();
        movingState.blockPos = entity.getBlockPos();
        movingState.blockState = camouflage;
        movingState.biome = clientLevel.getBiome(entity.getBlockPos());
        movingState.cardinalLighting = clientLevel.cardinalLighting();
        movingState.lightEngine = clientLevel.getLightEngine();
        state.camo = movingState;
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.camo != null) {
            collector.submitMovingBlock(poseStack, state.camo);
        }
    }
}
