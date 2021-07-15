package com.resimulators.simukraft;

import com.resimulators.simukraft.init.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class SimUTab extends ItemGroup {

    //creative tab
    public static SimUTab tab = new SimUTab("Sim-U-Kraft");

    private SimUTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModBlocks.CONSTRUCTOR_BOX.get());
    }
}
