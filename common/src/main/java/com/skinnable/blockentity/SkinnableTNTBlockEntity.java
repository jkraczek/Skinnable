package com.skinnable.blockentity;

import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class SkinnableTNTBlockEntity extends BlockEntity implements ICamouflageBlockEntity {

    @Nullable
    private BlockState camouflage;
    private int explosionPower = 4;
    private boolean wasPowered = false;

    public SkinnableTNTBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SKINNABLE_TNT.get(), pos, blockState);
    }

    @Override
    public @Nullable BlockState getCamouflage() { return camouflage; }

    @Override
    public void setCamouflage(BlockState state) {
        this.camouflage = state;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public BlockState getDefaultCamouflage() {
        return net.minecraft.world.level.block.Blocks.TNT.defaultBlockState();
    }

    public int getExplosionPower() { return explosionPower; }

    public void setExplosionPower(int power) {
        this.explosionPower = Math.max(1, Math.min(100, power));
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (camouflage != null) {
            output.store("Camouflage", BlockState.CODEC, camouflage);
        }
        output.putInt("ExplosionPower", explosionPower);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Camouflage", BlockState.CODEC).ifPresent(s -> camouflage = s);
        explosionPower = input.getIntOr("ExplosionPower", 4);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (camouflage != null) {
            tag.put("Camouflage", NbtUtils.writeBlockState(camouflage));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SkinnableTNTBlockEntity be) {
        if (!(level instanceof ServerLevel)) return;
        boolean nowPowered = level.hasNeighborSignal(pos);
        if (nowPowered && !be.wasPowered) {
            com.skinnable.block.SkinnableTNTBlock.prime(level, pos, be.explosionPower, null);
            return;
        }
        be.wasPowered = nowPowered;
    }
}
