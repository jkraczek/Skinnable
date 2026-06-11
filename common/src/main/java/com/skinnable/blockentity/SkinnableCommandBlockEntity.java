package com.skinnable.blockentity;

import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        if (level != null && !level.isClientSide()) {
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (camouflage != null) {
            output.store("Camouflage", BlockState.CODEC, camouflage);
        }
        output.putString("Mode", mode.name());
        output.putBoolean("Powered", powered);
        output.putBoolean("ConditionMet", conditionMet);
        output.putBoolean("Auto", auto);
        commandBlock.save(output);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Camouflage", BlockState.CODEC).ifPresent(s -> camouflage = s);
        try {
            mode = Mode.valueOf(input.getStringOr("Mode", mode.name()));
        } catch (IllegalArgumentException e) {
            mode = Mode.REDSTONE;
        }
        powered = input.getBooleanOr("Powered", false);
        conditionMet = input.getBooleanOr("ConditionMet", false);
        auto = input.getBooleanOr("Auto", false);
        commandBlock.load(input);
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

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, SkinnableCommandBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (be.mode == Mode.AUTO || (be.mode == Mode.REDSTONE && be.powered)) {
            be.commandBlock.performCommand(serverLevel);
        }
    }

    public class InnerCommandBlock extends BaseCommandBlock {

        @Override
        public void onUpdated(ServerLevel serverLevel) {
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
        public boolean isValid() {
            return SkinnableCommandBlockEntity.this.level != null
                    && SkinnableCommandBlockEntity.this.level instanceof ServerLevel;
        }

        @Override
        public CommandSourceStack createCommandSourceStack(ServerLevel serverLevel, CommandSource source) {
            BlockPos pos = SkinnableCommandBlockEntity.this.worldPosition;
            return new CommandSourceStack(
                    CommandSource.NULL,
                    Vec3.atCenterOf(pos),
                    Vec2.ZERO,
                    serverLevel,
                    PermissionSet.ALL_PERMISSIONS,
                    getName().getString(),
                    getName(),
                    serverLevel.getServer(),
                    null
            );
        }
    }
}
