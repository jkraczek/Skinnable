package com.skinnable;

import com.skinnable.registry.ModBlockEntityTypes;
import com.skinnable.registry.ModBlocks;
import com.skinnable.registry.ModCreativeTabs;
import com.skinnable.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Skinnable {
    public static final String MOD_ID = "skinnable";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ModBlocks.init();
        ModItems.init();
        ModBlockEntityTypes.init();
        ModCreativeTabs.init();
    }
}
