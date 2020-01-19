package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.sim.SimContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

public class OHRegistry {
    @ObjectHolder(Reference.MODID + ":sim_container")
    public static ContainerType<SimContainer> simContainer;
}
