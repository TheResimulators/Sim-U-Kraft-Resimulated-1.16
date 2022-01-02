package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {
    private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
    //Basic Blocks
    public static final RegistryObject<Block> COMPOSITE_BRICK = REGISTRY.register("composite_brick", () -> new BlockBase(AbstractBlock.Properties.of(Material.STONE, MaterialColor.STONE)));
    public static final RegistryObject<Block> CHEESE_BLOCK = REGISTRY.register("cheese_block", () -> new BlockBase(AbstractBlock.Properties.of(Material.SPONGE, MaterialColor.COLOR_YELLOW)));
    //Sim-u-kraft Structure Blocks
    public static final RegistryObject<Block> LIGHT_WHITE = REGISTRY.register("light_white", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_ORANGE = REGISTRY.register("light_orange", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_MAGENTA = REGISTRY.register("light_magenta", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_LIGHT_BLUE = REGISTRY.register("light_light_blue", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_YELLOW = REGISTRY.register("light_yellow", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_LIME = REGISTRY.register("light_lime", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_PINK = REGISTRY.register("light_pink", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_GRAY = REGISTRY.register("light_gray", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    //    public static final RegistryObject<Block> LIGHT_LIGHT_GRAY = REGISTRY.register("light_light_gray", () -> new BlockLight(Block.Properties.create(Material.WOOL))); //This should be a thing
    public static final RegistryObject<Block> LIGHT_CYAN = REGISTRY.register("light_cyan", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_PURPLE = REGISTRY.register("light_purple", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BLUE = REGISTRY.register("light_blue", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BROWN = REGISTRY.register("light_brown", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_GREEN = REGISTRY.register("light_green", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_RED = REGISTRY.register("light_red", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> LIGHT_BLACK = REGISTRY.register("light_black", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> CONSTRUCTOR_BOX = REGISTRY.register("constructor_box", () -> new BlockConstructor(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> CONTROL_BLOCK = REGISTRY.register("control_block", () -> new BlockControlBlock(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.COLOR_GRAY).harvestTool(ToolType.PICKAXE).noDrops().sound(SoundType.WOOD).strength(10f,10000f)));
    public static final RegistryObject<Block> FARM_BOX = REGISTRY.register("farm_box", () -> new BlockFarmBox(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY)));
    public static final RegistryObject<Block> RAINBOW_LIGHT = REGISTRY.register("rainbow_light", () -> new BlockLight(AbstractBlock.Properties.of(Material.WOOL)));
    public static final RegistryObject<Block> MINE_BOX = REGISTRY.register("mine_box", () -> new BlockMineBox(AbstractBlock.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> TERRAFORMER = REGISTRY.register("terraformer", () -> new BlockTerraformer(AbstractBlock.Properties.of(Material.WOOD)));
    //special blocks
    public static final RegistryObject<Block> MARKER = REGISTRY.register("marker", () -> new BlockMarker(AbstractBlock.Properties.of(Material.WOOD).harvestTool(ToolType.AXE).harvestLevel(1)));

    public ModBlocks() {
        REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
