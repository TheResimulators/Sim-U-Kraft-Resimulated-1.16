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
        getBuilder("rainbow_light").element().cube("#all").textureAll("block/rainbow_light");
        }

    @Override
    public String getName() {
        return "ItemModelGeneration";
    }
}
