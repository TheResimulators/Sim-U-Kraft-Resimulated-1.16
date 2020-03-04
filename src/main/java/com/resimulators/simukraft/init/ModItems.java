package com.resimulators.simukraft.init;
import com.resimulators.simukraft.SimUTab;
import com.resimulators.simukraft.SimuKraft;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    private static List<Item> REGISTRY = new ArrayList<>();

    public static final Item SIM_EGG = register(ModEntities.registerEntitySpawnegg(ModEntities.ENTITY_SIM, 0x07b351, 0x614500, "sim_egg"), SimUTab.tab);
    //public static final Item TEST_ITEM = register(new ItemTest(new Item.Properties()).setRegistryName(Reference.MODID, "test_item"), SimUTab.tab);

    public ModItems() {
        for (Block block : ModBlocks.getRegistry()) {
            register(block, SimUTab.tab);
        }
    }

    private static Item register(Block block, ItemGroup group) {
        if (block != null && block.getRegistryName() != null) {
            BlockItem temp = new BlockItem(block, new Item.Properties().group(group));
            REGISTRY.add(temp.setRegistryName(block.getRegistryName()));
            SimuKraft.LOGGER().info("Registered item: " + temp.getRegistryName());
            return temp;
        } else
            SimuKraft.LOGGER().error("Tried registering a item-block without a registry name. Skipping.");
        return null;
    }

    public static Item register(Item item, ItemGroup group) {
        if (item.getRegistryName() != null) {
            REGISTRY.add(item);
            SimuKraft.LOGGER().info("Registered block: " + item.getRegistryName().toString());
            return item;
        } else
            SimuKraft.LOGGER().error("Tried registering a item without a registry name. Skipping.");
        return null;
    }

    public static List<Item> getRegistry() {
        return REGISTRY;
    }
}
