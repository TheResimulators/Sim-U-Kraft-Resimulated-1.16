package com.Resimulators.simukraft.datagen;

import com.Resimulators.simukraft.Reference;
import net.minecraft.client.renderer.model.ItemModelGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;

public class ItemModelGeneration extends ItemModelProvider {

    public ItemModelGeneration(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Reference.MODID, existingFileHelper);
    }
    @Override
    protected void registerModels(){
        getBuilder("rainbow_light").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all","block/rainbow_light");
        getBuilder("mine_box").parent(getExistingFile(mcLoc("block/cube_all"))).texture("side","block/mine_box_side").texture("top","block/mine_box_top");
        getBuilder("composite_brick").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all","block/composite_brick");
        getBuilder("cheese_block").parent(getExistingFile(mcLoc("block/cube_all"))).texture("all","block/cheese_block");

        }

    @Override
    public String getName() {
        return "ItemModelGeneration";
    }
}
