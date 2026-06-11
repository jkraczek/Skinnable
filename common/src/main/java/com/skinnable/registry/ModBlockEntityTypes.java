package com.skinnable.registry;

import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
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
    }

    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BiFunction<BlockPos, BlockState, T> factory, Block block) {
        try {
            var ctors = BlockEntityType.class.getDeclaredConstructors();
            for (var ctor : ctors) {
                if (ctor.getParameterCount() == 2) {
                    ctor.setAccessible(true);
                    Class<?> supplierInterface = ctor.getParameterTypes()[0];
                    Object supplierProxy = Proxy.newProxyInstance(
                        supplierInterface.getClassLoader(),
                        new Class[]{supplierInterface},
                        (proxy, method, args) -> factory.apply((BlockPos) args[0], (BlockState) args[1])
                    );
                    return (BlockEntityType<T>) ctor.newInstance(supplierProxy, Set.of(block));
                }
            }
            throw new IllegalStateException("No suitable BlockEntityType constructor found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create BlockEntityType", e);
        }
    }
}
