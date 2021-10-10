package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimUTab;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.item.BanHammer;
import com.resimulators.simukraft.common.item.ItemStructureTest;
import com.resimulators.simukraft.common.item.JobEditorStick;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);
    //Item Blocks
    public static final RegistryObject<Item> LIGHT_WHITE = REGISTRY.register("light_white", () -> new BlockItem(ModBlocks.LIGHT_WHITE.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_ORANGE = REGISTRY.register("light_orange", () -> new BlockItem(ModBlocks.LIGHT_ORANGE.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_MAGENTA = REGISTRY.register("light_magenta", () -> new BlockItem(ModBlocks.LIGHT_MAGENTA.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_LIGHT_BLUE = REGISTRY.register("light_light_blue", () -> new BlockItem(ModBlocks.LIGHT_LIGHT_BLUE.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_YELLOW = REGISTRY.register("light_yellow", () -> new BlockItem(ModBlocks.LIGHT_YELLOW.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_LIME = REGISTRY.register("light_lime", () -> new BlockItem(ModBlocks.LIGHT_LIME.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_PINK = REGISTRY.register("light_pink", () -> new BlockItem(ModBlocks.LIGHT_PINK.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_GRAY = REGISTRY.register("light_gray", () -> new BlockItem(ModBlocks.LIGHT_GRAY.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_CYAN = REGISTRY.register("light_cyan", () -> new BlockItem(ModBlocks.LIGHT_CYAN.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_PURPLE = REGISTRY.register("light_purple", () -> new BlockItem(ModBlocks.LIGHT_PURPLE.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_BLUE = REGISTRY.register("light_blue", () -> new BlockItem(ModBlocks.LIGHT_BLUE.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_BROWN = REGISTRY.register("light_brown", () -> new BlockItem(ModBlocks.LIGHT_BROWN.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_GREEN = REGISTRY.register("light_green", () -> new BlockItem(ModBlocks.LIGHT_GREEN.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_RED = REGISTRY.register("light_red", () -> new BlockItem(ModBlocks.LIGHT_RED.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> LIGHT_BLACK = REGISTRY.register("light_black", () -> new BlockItem(ModBlocks.LIGHT_BLACK.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> COMPOSITE_BRICK = REGISTRY.register("composite_brick", () -> new BlockItem(ModBlocks.COMPOSITE_BRICK.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> CHEESE_BLOCK = REGISTRY.register("cheese_block", () -> new BlockItem(ModBlocks.CHEESE_BLOCK.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> CONSTRUCTOR_BOX = REGISTRY.register("constructor_box", () -> new BlockItem(ModBlocks.CONSTRUCTOR_BOX.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> CONTROL_BLOCK = REGISTRY.register("control_block", () -> new BlockItem(ModBlocks.CONTROL_BLOCK.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> FARM_BOX = REGISTRY.register("farm_box", () -> new BlockItem(ModBlocks.FARM_BOX.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> RAINBOW_LIGHT = REGISTRY.register("rainbow_light", () -> new BlockItem(ModBlocks.RAINBOW_LIGHT.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> MINE_BOX = REGISTRY.register("mine_box", () -> new BlockItem(ModBlocks.MINE_BOX.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> TERRAFORMER = REGISTRY.register("terraformer", () -> new BlockItem(ModBlocks.TERRAFORMER.get(), new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> MARKER = REGISTRY.register("marker", () -> new BlockItem(ModBlocks.MARKER.get(), new Item.Properties().tab(SimUTab.tab)));
    //Items
    //public static final Item TEST_ITEM = register(new ItemTest(new Item.Properties()).setRegistryName(Reference.MODID, "test_item"), SimUTab.tab);
    public static final RegistryObject<Item> STRUCTURE_TEST = REGISTRY.register("structure_test", () -> new ItemStructureTest(new Item.Properties().tab(SimUTab.tab)));
    public static final RegistryObject<Item> BAN_HAMMER = REGISTRY.register("ban_hammer", () -> new BanHammer(new Item.Properties().tab(SimUTab.tab).stacksTo(1)));
    public static final RegistryObject<Item> JOB_EDITOR_STICK = REGISTRY.register("job_editor_stick", () -> new JobEditorStick(new Item.Properties().tab(SimUTab.tab).stacksTo(1)));



    public ModItems() {
        REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}