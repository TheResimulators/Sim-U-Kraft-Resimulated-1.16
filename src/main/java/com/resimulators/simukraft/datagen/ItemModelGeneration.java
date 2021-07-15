package com.resimulators.simukraft.datagen;

import com.resimulators.simukraft.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGeneration extends ItemModelProvider {

    public ItemModelGeneration(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Reference.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        getBuilder("rainbow_light").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all", "block/rainbow_light");
        getBuilder("mine_box").parent(getExistingFile(mcLoc("block/cube_all"))).texture("side", "block/mine_box_side").texture("top", "block/mine_box_top");
        getBuilder("composite_brick").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all", "block/composite_brick");
        getBuilder("cheese_block").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all", "block/cheese_block");

    }

    @Override
    public String getName() {
        return "ItemModelGeneration";
    }
}
