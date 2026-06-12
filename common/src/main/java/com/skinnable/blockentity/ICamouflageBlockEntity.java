package com.skinnable.blockentity;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ICamouflageBlockEntity {
    @Nullable BlockState getCamouflage();
    void setCamouflage(BlockState state);

    default @Nullable BlockState getDefaultCamouflage() { return null; }
}
