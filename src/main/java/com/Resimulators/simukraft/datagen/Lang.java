package com.Resimulators.simukraft.datagen;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.SimUTab;
import com.Resimulators.simukraft.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.data.LanguageProvider;

public class Lang extends LanguageProvider {
    public Lang(DataGenerator gen) {
        super(gen, Reference.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.CONSTRUCTOR_BOX, "Constructor Box");
        add(ModBlocks.CONTROL_BOX, "Control Box");
        add(ModBlocks.FARM_BOX, "Farm Box");
        for (DyeColor color : DyeColor.values()) {
            if (color.equals(DyeColor.LIGHT_GRAY)) continue;
            add(ModBlocks.LIGHT_BLOCKS.get("light_" + color.toString()), color.toString() + " Light");
        }
        add(ModBlocks.MINE_BOX, "Mine Box");
        add(ModBlocks.CHEESE_BLOCK, "Cheese Block");
        add(ModBlocks.COMPOSITE_BRICK, "Composite Brick");
        add(ModBlocks.RAINBOW_LIGHT, "Rainbow Light");
        add(SimUTab.tab.getTranslationKey(), "Sim-U-Kraft");


    }
}
