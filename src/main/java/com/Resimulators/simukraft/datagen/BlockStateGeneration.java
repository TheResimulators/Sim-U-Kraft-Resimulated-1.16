package com.Resimulators.simukraft.datagen;


import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.awt.*;


public class BlockStateGeneration extends BlockStateProvider {


    public BlockStateGeneration(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Reference.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile rainbowLight = cubeAll(ModBlocks.RAINBOW_LIGHT);
        simpleBlock(ModBlocks.RAINBOW_LIGHT,rainbowLight);

        ModelFile mineBox = cubeColumn("mine_box",new ResourceLocation("simukraft:block/mine_box_side"),new ResourceLocation("simukraft:block/mine_box_top"));
        simpleBlock(ModBlocks.MINE_BOX,mineBox);

        ModelFile compositeBrick = cubeAll(ModBlocks.COMPOSITE_BRICK);
        simpleBlock(ModBlocks.COMPOSITE_BRICK,compositeBrick);

        ModelFile cheeseBlock = cubeAll(ModBlocks.CHEESE_BLOCK);
        simpleBlock(ModBlocks.CHEESE_BLOCK,cheeseBlock);
        for (DyeColor color : DyeColor.values()) {
            if (color.equals(DyeColor.LIGHT_GRAY))continue;
            ModelFile coloredBlock = cubeAll("light_" + color.toString(),new ResourceLocation("simukraft:block/light_"+color));
            simpleBlock(ModBlocks.LIGHT_BLOCKS.get("light_"+color.toString()),coloredBlock);

        }
    }
}
