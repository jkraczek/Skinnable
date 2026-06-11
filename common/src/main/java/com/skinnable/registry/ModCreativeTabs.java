package com.skinnable.registry;

import com.skinnable.Skinnable;
import com.skinnable.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static Supplier<CreativeModeTab> SKINNABLE_TAB;

    @SuppressWarnings("unchecked")
    public static void init() {
        SKINNABLE_TAB = (Supplier<CreativeModeTab>)(Supplier<?>) Services.PLATFORM.register(
                BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                "skinnable",
                () -> CreativeModeTab.builder(Row.TOP, 0)
                        .title(Component.translatable("itemGroup." + Skinnable.MOD_ID))
                        .icon(() -> new ItemStack(Blocks.LIME_CONCRETE))
                        .build()
        );
    }
}
