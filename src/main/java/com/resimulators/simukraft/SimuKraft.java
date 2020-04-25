package com.resimulators.simukraft;

import com.resimulators.simukraft.client.data.SkinCacher;
import com.resimulators.simukraft.client.gui.GuiMod;
import com.resimulators.simukraft.client.gui.GuiSimInventory;
import com.resimulators.simukraft.client.gui.SimHud;
import com.resimulators.simukraft.client.render.MarkerEntityRender;
import com.resimulators.simukraft.common.entity.sim.EntitySim;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
import com.resimulators.simukraft.common.entity.sim.SimInformationOverlay;
import com.resimulators.simukraft.common.events.world.MarkerBrokenEvent;
import com.resimulators.simukraft.common.events.world.NewDayEvent;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.*;
import com.resimulators.simukraft.init.ModBlocks;
import com.resimulators.simukraft.init.ModEntities;
import com.resimulators.simukraft.init.ModItems;
import com.resimulators.simukraft.init.ModRenders;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MODID)
public class SimuKraft {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public SimuKraft() {
        //Registering Configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.COMMON_CONFIG);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        //Add config events
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Configs::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Configs::onFileChange);

        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen));

        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static Logger LOGGER() {
        return LOGGER;
    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new NewDayEvent());
        MinecraftForge.EVENT_BUS.register(new FactionEvents());
        MinecraftForge.EVENT_BUS.register(MarkerBrokenEvent.class);
        Network.handler.init();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        MinecraftForge.EVENT_BUS.register(new SimHud());
        MinecraftForge.EVENT_BUS.register(new SimInformationOverlay());
        MarkerEntityRender.register();
        //Registering SkinCache and Special Skins

        SkinCacher skinCacher = new SkinCacher();
        skinCacher.initSkinService();
        skinCacher.registerSpecialSkins();

        ModRenders.registerEntityRenders();

        ScreenManager.registerFactory(OHRegistry.simContainer, GuiSimInventory::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        StructureHandler.createTemplateManager(event.getServer(), event.getServer().getDataDirectory());
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            new ModBlocks();
            for (Block block : ModBlocks.getRegistry()) {
                event.getRegistry().register(block);
            }
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
            new ModTileEntities();
            for (TileEntityType type : ModTileEntities.getRegistry()) {
                event.getRegistry().register(type);
            }
        }

        @SubscribeEvent
        public static void OnItemEntityRegistry(final RegistryEvent.Register<Item> event) {
            new ModItems();
            for (Item item : ModItems.getRegistry()) {
                event.getRegistry().register(item);
            }
        }


        @SubscribeEvent
        public static void OnEntityRegistry(final RegistryEvent.Register<EntityType<?>> entityRegisterEvent) {
            ModEntities.init(entityRegisterEvent);
        }

        @SubscribeEvent
        public static void onContainerRegistry(RegistryEvent.Register<ContainerType<?>> event) {
            IForgeRegistry<ContainerType<?>> r = event.getRegistry();
            r.register(IForgeContainerType.create((windowId, inv, data) -> new SimContainer(windowId,false, new EntitySim(ModEntities.ENTITY_SIM, null), inv)).setRegistryName(Reference.MODID, "sim_container"));
        }
    }
}

