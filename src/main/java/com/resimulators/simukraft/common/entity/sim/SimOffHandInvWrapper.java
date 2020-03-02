package com.resimulators.simukraft.common.entity.sim;

import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

public class SimOffHandInvWrapper extends RangedWrapper {
    public SimOffHandInvWrapper(SimInventory inventory) {
        super(new InvWrapper(inventory), inventory.mainInventory.size() + inventory.armorInventory.size(), inventory.mainInventory.size() + inventory.armorInventory.size() + inventory.handInventory.size());
    }
}
