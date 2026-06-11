package com.skinnable.block;

import com.mojang.serialization.MapCodec;
import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SkinnableCommandBlock extends BaseEntityBlock {

    public static final MapCodec<SkinnableCommandBlock> CODEC = simpleCodec(SkinnableCommandBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SkinnableCommandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkinnableCommandBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(),
                SkinnableCommandBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().instabuild) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SkinnableCommandBlockEntity cmdBe)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof BlockItem blockItem) {
            BlockState camouflage = blockItem.getBlock().defaultBlockState();
            cmdBe.setCamouflage(camouflage);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, net.minecraft.core.BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide()) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SkinnableCommandBlockEntity cmdBe)) return;

        boolean newPowered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (newPowered != wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, newPowered), 3);
            cmdBe.setPowered(newPowered);

            if (newPowered && cmdBe.getMode() == SkinnableCommandBlockEntity.Mode.REDSTONE
                    && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                cmdBe.getCommandBlock().performCommand(serverLevel);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.getAbilities().instabuild) {
            level.setBlock(pos, state, 3);
            return state;
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!player.getAbilities().instabuild) return 0.0F;
        return super.getDestroyProgress(state, player, level, pos);
    }
}
