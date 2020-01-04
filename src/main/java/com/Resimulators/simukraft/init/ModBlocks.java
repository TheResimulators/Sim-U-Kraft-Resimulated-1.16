package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.block.BlockConstructor;
import com.Resimulators.simukraft.common.block.BlockControlBox;
import com.Resimulators.simukraft.common.block.BlockFarmBox;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
public final class ModBlocks {



    //Sim-u-kraft Structure Blocks
    public static final Block CONSTRUCTOR_BOX = new BlockConstructor(Block.Properties.create(Material.WOOD, MaterialColor.BROWN),"constructor_box");
    public static final Block CONTROL_BOX = new BlockControlBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY),"control_box");
    public static final Block FARM_BOX = new BlockFarmBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY),"farm_box");




    public static void init(final RegistryEvent.Register<Block> event){

            event.getRegistry().register(CONSTRUCTOR_BOX);
            event.getRegistry().register(CONTROL_BOX);
            event.getRegistry().register(FARM_BOX);



    }


}
