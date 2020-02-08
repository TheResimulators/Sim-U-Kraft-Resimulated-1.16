package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.SimuKraft;
import com.Resimulators.simukraft.common.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ObjectHolder(Reference.MODID)
public final class ModBlocks {
    private static List<Block> REGISTRY = new ArrayList<>();
    //Basic Blocks
    public static final Block COMPOSITE_BRICK = register(new BlockBase(Block.Properties.create(Material.ROCK,MaterialColor.STONE),"composite_brick"));
    public static final Block CHEESE_BLOCK = register(new BlockBase(Block.Properties.create(Material.SPONGE,MaterialColor.YELLOW),"cheese_block"));

    //Sim-u-kraft Structure Blocks
    public static Map<String, Block> LIGHT_BLOCKS = new HashMap<>();
    public static final Block CONSTRUCTOR_BOX = register(new BlockConstructor(Block.Properties.create(Material.WOOD, MaterialColor.BROWN).harvestTool(ToolType.AXE),"constructor_box"));
    public static final Block CONTROL_BOX = register(new BlockControlBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY).harvestTool(ToolType.PICKAXE),"control_box"));
    public static final Block FARM_BOX = register(new BlockFarmBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY),"farm_box"));
    public static final Block RAINBOW_LIGHT = register(new BlockLight(Block.Properties.create(Material.WOOL),"rainbow_light"));
    public static final Block MINE_BOX = register(new BlockMineBox(Block.Properties.create(Material.WOOD),"mine_box"));

    //special blocks
    public static final Block MARKER = register(new BlockMarker(Block.Properties.create(Material.WOOD).harvestTool(ToolType.AXE).harvestLevel(1),"marker"));

    public ModBlocks() {
        for (DyeColor color : DyeColor.values()) {
            if (color.equals(DyeColor.LIGHT_GRAY))continue;
            BlockLight LIGHT = new BlockLight(Block.Properties.create(Material.WOOL),"light_"+color.toString());
            LIGHT_BLOCKS.put("light_"+color.toString(),LIGHT);
            register(LIGHT);
        }
    }

    private static Block register(Block block) {
        if (block.getRegistryName() != null) {
            REGISTRY.add(block);
            SimuKraft.LOGGER().info("Registered block: " + block.getRegistryName().toString());
            return block;
        } else
            SimuKraft.LOGGER().error("Tried registering a block without a registry name. Skipping.");
        return null;
    }

    public static List<Block> getRegistry() {
        return REGISTRY;
    }
}
