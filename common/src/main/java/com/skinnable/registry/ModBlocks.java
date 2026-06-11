package com.skinnable.registry;

import com.skinnable.block.SkinnableCommandBlock;
import com.skinnable.block.SkinnableSpawnerBlock;
import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ModBlocks {
    public static Supplier<Block> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<Block> SKINNABLE_SPAWNER;

    public static void init() {
        SKINNABLE_COMMAND_BLOCK = Services.PLATFORM.register(
                BuiltInRegistries.BLOCK.key(),
                "skinnable_command_block",
                () -> new SkinnableCommandBlock(BlockBehaviour.Properties.of()
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion())
        );
        SKINNABLE_SPAWNER = Services.PLATFORM.register(
                BuiltInRegistries.BLOCK.key(),
                "skinnable_spawner",
                () -> new SkinnableSpawnerBlock(BlockBehaviour.Properties.of()
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion())
        );
    }
}
