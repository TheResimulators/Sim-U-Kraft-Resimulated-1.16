package com.resimulators.simukraft.datagen;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimUTab;
import com.resimulators.simukraft.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class Lang extends LanguageProvider {
    public Lang(DataGenerator gen) {
        super(gen, Reference.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.CONSTRUCTOR_BOX.get(), "Constructor Box");
        add(ModBlocks.CONTROL_BLOCK.get(), "Control Box");
        add(ModBlocks.FARM_BOX.get(), "Farm Box");
        add(ModBlocks.MINE_BOX.get(), "Mine Box");
        add(ModBlocks.CHEESE_BLOCK.get(), "Cheese Block");
        add(ModBlocks.COMPOSITE_BRICK.get(), "Composite Brick");
        add(ModBlocks.RAINBOW_LIGHT.get(), "Rainbow Light");
        add(ModBlocks.LIGHT_WHITE.get(), "White Light");
        add(ModBlocks.LIGHT_ORANGE.get(), "Orange Light");
        add(ModBlocks.LIGHT_MAGENTA.get(), "Magenta Light");
        add(ModBlocks.LIGHT_LIGHT_BLUE.get(), "Light Blue Light");
        add(ModBlocks.LIGHT_YELLOW.get(), "Yellow Light");
        add(ModBlocks.LIGHT_LIME.get(), "Lime Light");
        add(ModBlocks.LIGHT_PINK.get(), "Pink Light");
        add(ModBlocks.LIGHT_GRAY.get(), "Gray Light");
//        add(ModBlocks.LIGHT_LIGHT_GRAY.get(), "Light Gray Light");
        add(ModBlocks.LIGHT_CYAN.get(), "Cyan Light");
        add(ModBlocks.LIGHT_PURPLE.get(), "Purple Light");
        add(ModBlocks.LIGHT_BLUE.get(), "Blue Light");
        add(ModBlocks.LIGHT_BROWN.get(), "Brown Light");
        add(ModBlocks.LIGHT_GREEN.get(), "Green Light");
        add(ModBlocks.LIGHT_RED.get(), "Red Light");
        add(ModBlocks.LIGHT_BLACK.get(), "Black Light");
        add(SimUTab.tab.getRecipeFolderName(), "Sim-U-Kraft");


    }
}
