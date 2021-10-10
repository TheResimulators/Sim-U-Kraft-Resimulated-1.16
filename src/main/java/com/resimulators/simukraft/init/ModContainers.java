package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiSimInventory;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Reference.MODID);
    public static RegistryObject<ContainerType<SimContainer>> SIM_CONTAINER = CONTAINERS.register("sim", () -> IForgeContainerType.create(((windowId, inv, data) -> new SimContainer(windowId, false, new SimEntity(SimuKraft.proxy.getClientWorld()), inv))));

    public static void registerScreens() {
        ScreenManager.register(SIM_CONTAINER.get(), GuiSimInventory::new);
    }

    public ModContainers() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
