package com.skinnable.registry;

import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static Supplier<BlockEntityType<SkinnableCommandBlockEntity>> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<BlockEntityType<SkinnableSpawnerBlockEntity>> SKINNABLE_SPAWNER;

    @SuppressWarnings("unchecked")
    public static void init() {
        SKINNABLE_COMMAND_BLOCK = (Supplier<BlockEntityType<SkinnableCommandBlockEntity>>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                "skinnable_command_block",
                () -> BlockEntityType.Builder.of(
                        SkinnableCommandBlockEntity::new,
                        ModBlocks.SKINNABLE_COMMAND_BLOCK.get()
                ).build(null)
        );
        SKINNABLE_SPAWNER = (Supplier<BlockEntityType<SkinnableSpawnerBlockEntity>>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                "skinnable_spawner",
                () -> BlockEntityType.Builder.of(
                        SkinnableSpawnerBlockEntity::new,
                        ModBlocks.SKINNABLE_SPAWNER.get()
                ).build(null)
        );
    }
}
