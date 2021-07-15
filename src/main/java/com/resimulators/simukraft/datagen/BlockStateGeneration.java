package com.resimulators.simukraft.datagen;


import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;


public class BlockStateGeneration extends BlockStateProvider {


    public BlockStateGeneration(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Reference.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile rainbowLight = cubeAll(ModBlocks.RAINBOW_LIGHT.get());
        simpleBlock(ModBlocks.RAINBOW_LIGHT.get(), rainbowLight);

        ModelFile compositeBrick = cubeAll(ModBlocks.COMPOSITE_BRICK.get());
        simpleBlock(ModBlocks.COMPOSITE_BRICK.get(), compositeBrick);

        ModelFile cheeseBlock = cubeAll(ModBlocks.CHEESE_BLOCK.get());
        simpleBlock(ModBlocks.CHEESE_BLOCK.get(), cheeseBlock);
        for (DyeColor color : DyeColor.values()) {
            if (color.equals(DyeColor.LIGHT_GRAY)) continue;

        }
    }
}
