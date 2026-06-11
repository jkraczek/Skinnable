package com.skinnable.blockentity;

import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SkinnableCommandBlockEntity extends BlockEntity implements ICamouflageBlockEntity {

    public enum Mode { REDSTONE, AUTO, SEQUENCE }

    @Nullable
    private BlockState camouflage;
    private Mode mode = Mode.REDSTONE;
    private boolean powered = false;
    private boolean conditionMet = false;
    private boolean auto = false;

    private final InnerCommandBlock commandBlock = new InnerCommandBlock();

    public SkinnableCommandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(), pos, blockState);
    }

    @Override
    public @Nullable BlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(BlockState state) {
        this.camouflage = state;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BaseCommandBlock getCommandBlock() {
        return commandBlock;
    }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }
    public boolean isPowered() { return powered; }
    public void setPowered(boolean powered) { this.powered = powered; }
    public boolean isAuto() { return auto; }
    public void setAuto(boolean auto) { this.auto = auto; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (camouflage != null) {
            tag.put("Camouflage", NbtUtils.writeBlockState(camouflage));
        }
        tag.putString("Mode", mode.name());
        tag.putBoolean("Powered", powered);
        tag.putBoolean("ConditionMet", conditionMet);
        tag.putBoolean("Auto", auto);
        commandBlock.save(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Camouflage")) {
            camouflage = NbtUtils.readBlockState(
                    registries.lookupOrThrow(Registries.BLOCK),
                    tag.getCompound("Camouflage")
            );
        }
        if (tag.contains("Mode")) {
            try {
                mode = Mode.valueOf(tag.getString("Mode"));
            } catch (IllegalArgumentException e) {
                mode = Mode.REDSTONE;
            }
        }
        powered = tag.getBoolean("Powered");
        conditionMet = tag.getBoolean("ConditionMet");
        auto = tag.getBoolean("Auto");
        commandBlock.load(tag);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, SkinnableCommandBlockEntity be) {
        if (!(level instanceof ServerLevel)) return;
        if (be.mode == Mode.AUTO || (be.mode == Mode.REDSTONE && be.powered)) {
            be.commandBlock.performCommand((ServerLevel) level);
        }
    }

    public class InnerCommandBlock extends BaseCommandBlock {

        @Override
        public ServerLevel getLevel() {
            return (ServerLevel) SkinnableCommandBlockEntity.this.level;
        }

        @Override
        public Component getName() {
            return Component.literal("@");
        }

        @Override
        public void onUpdated() {
            SkinnableCommandBlockEntity.this.setChanged();
            if (SkinnableCommandBlockEntity.this.level != null) {
                SkinnableCommandBlockEntity.this.level.sendBlockUpdated(
                        SkinnableCommandBlockEntity.this.worldPosition,
                        SkinnableCommandBlockEntity.this.getBlockState(),
                        SkinnableCommandBlockEntity.this.getBlockState(),
                        3
                );
            }
        }

        @Override
        public CommandSourceStack createCommandSourceStack() {
            ServerLevel serverLevel = getLevel();
            BlockPos pos = SkinnableCommandBlockEntity.this.worldPosition;
            return new CommandSourceStack(
                    CommandSource.NULL,
                    Vec3.atCenterOf(pos),
                    Vec2.ZERO,
                    serverLevel,
                    2,
                    getName().getString(),
                    getName(),
                    serverLevel.getServer(),
                    null
            );
        }
    }
}
