package com.skinnable.block;

import com.mojang.serialization.MapCodec;
import com.skinnable.blockentity.SkinnableTNTBlockEntity;
import com.skinnable.network.packet.S2COpenTNTScreenPacket;
import com.skinnable.platform.Services;
import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SkinnableTNTBlock extends BaseEntityBlock {

    public static final MapCodec<SkinnableTNTBlock> CODEC = simpleCodec(SkinnableTNTBlock::new);

    public SkinnableTNTBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkinnableTNTBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SKINNABLE_TNT.get(),
                SkinnableTNTBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().instabuild) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SkinnableTNTBlockEntity tntBe)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof BlockItem blockItem) {
            tntBe.setCamouflage(blockItem.getBlock().defaultBlockState());
            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty() && player instanceof ServerPlayer sp) {
            Services.PLATFORM.sendPacketToPlayer(sp, new S2COpenTNTScreenPacket(pos, tntBe.getExplosionPower()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        BlockEntity be = level.getBlockEntity(pos);
        int power = (be instanceof SkinnableTNTBlockEntity tntBe) ? tntBe.getExplosionPower() : 4;
        Entity source = explosion.getIndirectSourceEntity();
        LivingEntity igniter = (source instanceof LivingEntity living) ? living : null;
        PrimedTnt tnt = createPrimedTnt(level, pos, igniter, power);
        // Shorter random fuse when caught in another explosion, same as vanilla TNT
        var rng = level.getRandom();
        tnt.setFuse(rng.nextInt(rng.nextInt(rng.nextInt(100) + 1) + 1));
        level.addFreshEntity(tnt);
        level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide() && projectile.isOnFire()) {
            BlockPos pos = hit.getBlockPos();
            Entity shooter = projectile.getOwner();
            LivingEntity igniter = (shooter instanceof LivingEntity living) ? living : null;
            BlockEntity be = level.getBlockEntity(pos);
            int power = (be instanceof SkinnableTNTBlockEntity tntBe) ? tntBe.getExplosionPower() : 4;
            prime(level, pos, power, igniter);
        }
    }

    public static void prime(Level level, BlockPos pos, int power, @Nullable LivingEntity igniter) {
        if (level.isClientSide()) return;
        PrimedTnt tnt = createPrimedTnt(level, pos, igniter, power);
        level.removeBlock(pos, false);
        level.addFreshEntity(tnt);
        level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
    }

    private static PrimedTnt createPrimedTnt(Level level, BlockPos pos, @Nullable LivingEntity igniter, int power) {
        PrimedTnt tnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
        // Set custom explosion power via reflection since PrimedTnt has no public setter
        try {
            java.lang.reflect.Field f = PrimedTnt.class.getDeclaredField("explosionPower");
            f.setAccessible(true);
            f.setFloat(tnt, (float) power);
        } catch (Exception ignored) {}
        return tnt;
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
