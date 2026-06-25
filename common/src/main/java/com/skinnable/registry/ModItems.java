package com.skinnable.registry;

import com.skinnable.Skinnable;
import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<Item> SKINNABLE_COMMAND_BLOCK;
    public static Supplier<Item> SKINNABLE_SPAWNER;
    public static Supplier<Item> SKINNABLE_TNT;

    public static void init() {
        SKINNABLE_COMMAND_BLOCK = Services.PLATFORM.register(
                BuiltInRegistries.ITEM.key(),
                "skinnable_command_block",
                () -> new BlockItem(ModBlocks.SKINNABLE_COMMAND_BLOCK.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_command_block"))))
        );
        SKINNABLE_SPAWNER = Services.PLATFORM.register(
                BuiltInRegistries.ITEM.key(),
                "skinnable_spawner",
                () -> new BlockItem(ModBlocks.SKINNABLE_SPAWNER.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_spawner"))))
        );
        SKINNABLE_TNT = Services.PLATFORM.register(
                BuiltInRegistries.ITEM.key(),
                "skinnable_tnt",
                () -> new BlockItem(ModBlocks.SKINNABLE_TNT.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable_tnt"))))
        );
    }
}
