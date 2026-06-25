package com.skinnable.registry;

import com.skinnable.Skinnable;
import com.skinnable.block.SkinnableCommandBlock;
import com.skinnable.block.SkinnableSpawnerBlock;
import com.skinnable.block.SkinnableTNTBlock;
import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ModBlocks {
    public static Supplier<Block> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<Block> SKINNABLE_SPAWNER;
    public static Supplier<Block> SKINNABLE_TNT;

    public static void init() {
        SKINNABLE_COMMAND_BLOCK = Services.PLATFORM.register(
                BuiltInRegistries.BLOCK.key(),
                "skinnable_command_block",
                () -> new SkinnableCommandBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_command_block")))
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion())
        );
        SKINNABLE_SPAWNER = Services.PLATFORM.register(
                BuiltInRegistries.BLOCK.key(),
                "skinnable_spawner",
                () -> new SkinnableSpawnerBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_spawner")))
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion())
        );
        SKINNABLE_TNT = Services.PLATFORM.register(
                BuiltInRegistries.BLOCK.key(),
                "skinnable_tnt",
                () -> new SkinnableTNTBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_tnt")))
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.GRASS)
                        .noOcclusion())
        );
    }
}
