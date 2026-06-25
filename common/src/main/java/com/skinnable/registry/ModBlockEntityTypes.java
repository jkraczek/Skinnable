package com.skinnable.registry;

import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.blockentity.SkinnableTNTBlockEntity;
import com.skinnable.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static Supplier<BlockEntityType<SkinnableCommandBlockEntity>> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<BlockEntityType<SkinnableSpawnerBlockEntity>> SKINNABLE_SPAWNER;
    public static Supplier<BlockEntityType<SkinnableTNTBlockEntity>> SKINNABLE_TNT;

    @SuppressWarnings("unchecked")
    public static void init() {
        SKINNABLE_COMMAND_BLOCK = (Supplier<BlockEntityType<SkinnableCommandBlockEntity>>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                "skinnable_command_block",
                () -> createBlockEntityType(SkinnableCommandBlockEntity::new, ModBlocks.SKINNABLE_COMMAND_BLOCK.get())
        );
        SKINNABLE_SPAWNER = (Supplier<BlockEntityType<SkinnableSpawnerBlockEntity>>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                "skinnable_spawner",
                () -> createBlockEntityType(SkinnableSpawnerBlockEntity::new, ModBlocks.SKINNABLE_SPAWNER.get())
        );
        SKINNABLE_TNT = (Supplier<BlockEntityType<SkinnableTNTBlockEntity>>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                "skinnable_tnt",
                () -> createBlockEntityType(SkinnableTNTBlockEntity::new, ModBlocks.SKINNABLE_TNT.get())
        );
    }

    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BiFunction<BlockPos, BlockState, T> factory, Block block) {
        Exception lastError = null;
        for (var ctor : BlockEntityType.class.getDeclaredConstructors()) {
            int paramCount = ctor.getParameterCount();
            if (paramCount < 2) continue;
            try {
                ctor.setAccessible(true);
                Class<?> supplierInterface = ctor.getParameterTypes()[0];
                if (!supplierInterface.isInterface()) continue;
                Object supplierProxy = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{supplierInterface},
                    (proxy, method, args) -> {
                        if (args != null && args.length >= 2)
                            return factory.apply((BlockPos) args[0], (BlockState) args[1]);
                        return null;
                    }
                );
                Object[] args = new Object[paramCount];
                args[0] = supplierProxy;
                args[1] = Set.of(block);
                return (BlockEntityType<T>) ctor.newInstance(args);
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw new RuntimeException("Failed to create BlockEntityType", lastError);
    }
}
