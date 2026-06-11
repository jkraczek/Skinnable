package com.skinnable.block;

import com.mojang.serialization.MapCodec;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.platform.Services;
import com.skinnable.registry.ModBlockEntityTypes;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SkinnableSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<SkinnableSpawnerBlock> CODEC = simpleCodec(SkinnableSpawnerBlock::new);

    public SkinnableSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkinnableSpawnerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SKINNABLE_SPAWNER.get(),
                SkinnableSpawnerBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().instabuild) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SkinnableSpawnerBlockEntity spawnerBe)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof BlockItem blockItem) {
            BlockState camouflage = blockItem.getBlock().defaultBlockState();
            spawnerBe.setCamouflage(camouflage);
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            Services.PLATFORM.sendPacketToPlayer(sp, new S2COpenSpawnerScreenPacket(pos));
        }
        return InteractionResult.SUCCESS;
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
