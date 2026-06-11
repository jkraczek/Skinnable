package com.skinnable.registry;

import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<Item> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<Item> SKINNABLE_SPAWNER;

    public static void init() {
        SKINNABLE_COMMAND_BLOCK = Services.PLATFORM.register(
                BuiltInRegistries.ITEM.key(),
                "skinnable_command_block",
                () -> new BlockItem(ModBlocks.SKINNABLE_COMMAND_BLOCK.get(), new Item.Properties())
        );
        SKINNABLE_SPAWNER = Services.PLATFORM.register(
                BuiltInRegistries.ITEM.key(),
                "skinnable_spawner",
                () -> new BlockItem(ModBlocks.SKINNABLE_SPAWNER.get(), new Item.Properties())
        );
    }
}
