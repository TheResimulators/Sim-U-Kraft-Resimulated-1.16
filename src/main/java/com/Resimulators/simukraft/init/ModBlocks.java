package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

import java.util.HashMap;
import java.util.Map;

@ObjectHolder(Reference.MODID)
public final class ModBlocks {

    //Basic Blocks
    public static final Block COMPOSITE_BRICK = new BlockBase(Block.Properties.create(Material.ROCK,MaterialColor.STONE),"composite_brick");
    public static final Block CHEESE_BLOCK = new BlockBase(Block.Properties.create(Material.SPONGE,MaterialColor.YELLOW),"cheese_block");

    //Sim-u-kraft Structure Blocks
    public static Map<String,Block> LIGHT_BLOCKS = new HashMap<>();
    public static final Block CONSTRUCTOR_BOX = new BlockConstructor(Block.Properties.create(Material.WOOD, MaterialColor.BROWN),"constructor_box");
    public static final Block CONTROL_BOX = new BlockControlBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY),"control_box");
    public static final Block FARM_BOX = new BlockFarmBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY),"farm_box");
    public static final Block RAINBOW_LIGHT = new BlockLight(Block.Properties.create(Material.WOOL),"rainbow_light");
    public static final Block MINE_BOX = new BlockMineBox(Block.Properties.create(Material.WOOD),"mine_box");




    public static void init(final RegistryEvent.Register<Block> event){
        event.getRegistry().register(CONSTRUCTOR_BOX);
        event.getRegistry().register(CONTROL_BOX);
        event.getRegistry().register(FARM_BOX);
        event.getRegistry().register(MINE_BOX);
        event.getRegistry().register(COMPOSITE_BRICK);
        event.getRegistry().register(CHEESE_BLOCK);

        for (DyeColor color : DyeColor.values()) {
            if (color.equals(DyeColor.LIGHT_GRAY))continue;
            BlockLight LIGHT = new BlockLight(Block.Properties.create(Material.WOOL),"light_"+color.toString());
            LIGHT_BLOCKS.put("light_"+color.toString(),LIGHT);
            event.getRegistry().register(LIGHT);
        }
        event.getRegistry().register(RAINBOW_LIGHT);


    }


}
