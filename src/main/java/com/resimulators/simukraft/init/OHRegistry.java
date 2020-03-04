package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

public class OHRegistry {
    @ObjectHolder(Reference.MODID + ":sim_container")
    public static ContainerType<SimContainer> simContainer;
}
