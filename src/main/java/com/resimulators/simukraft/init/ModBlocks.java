package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModBlocks {
    private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);

    public ModBlocks() {
        REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //Basic Blocks
    public static final RegistryObject<Block> COMPOSITE_BRICK = REGISTRY.register("composite_brick", () -> new BlockBase(Block.Properties.create(Material.ROCK, MaterialColor.STONE)));
    public static final RegistryObject<Block> CHEESE_BLOCK = REGISTRY.register("cheese_block", () -> new BlockBase(Block.Properties.create(Material.SPONGE,MaterialColor.YELLOW)));

    //Sim-u-kraft Structure Blocks
    public static final RegistryObject<Block> LIGHT_WHITE = REGISTRY.register("light_white", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_ORANGE = REGISTRY.register("light_orange", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_MAGENTA = REGISTRY.register("light_magenta", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_LIGHT_BLUE = REGISTRY.register("light_light_blue", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_YELLOW = REGISTRY.register("light_yellow", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_LIME = REGISTRY.register("light_lime", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_PINK = REGISTRY.register("light_pink", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_GRAY = REGISTRY.register("light_gray", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
//    public static final RegistryObject<Block> LIGHT_LIGHT_GRAY = REGISTRY.register("light_light_gray", () -> new BlockLight(Block.Properties.create(Material.WOOL))); //This should be a thing
    public static final RegistryObject<Block> LIGHT_CYAN = REGISTRY.register("light_cyan", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_PURPLE = REGISTRY.register("light_purple", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BLUE = REGISTRY.register("light_blue", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BROWN = REGISTRY.register("light_brown", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_GREEN = REGISTRY.register("light_green", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_RED = REGISTRY.register("light_red", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BLACK = REGISTRY.register("light_black", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> CONSTRUCTOR_BOX = REGISTRY.register("constructor_box", () -> new BlockConstructor(Block.Properties.create(Material.WOOD, MaterialColor.BROWN).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> CONTROL_BOX = REGISTRY.register("control_box", () -> new BlockControlBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY).harvestTool(ToolType.PICKAXE)));
    public static final RegistryObject<Block> FARM_BOX = REGISTRY.register("farm_box", () -> new BlockFarmBox(Block.Properties.create(Material.ROCK,MaterialColor.GRAY)));
    public static final RegistryObject<Block> RAINBOW_LIGHT = REGISTRY.register("rainbow_light", () -> new BlockLight(Block.Properties.create(Material.WOOL)));
    public static final RegistryObject<Block> MINE_BOX = REGISTRY.register("mine_box", () -> new BlockMineBox(Block.Properties.create(Material.WOOD)));

    //special blocks
    public static final RegistryObject<Block> MARKER = REGISTRY.register("marker", () -> new BlockMarker(Block.Properties.create(Material.WOOD).harvestTool(ToolType.AXE).harvestLevel(1)));
}
