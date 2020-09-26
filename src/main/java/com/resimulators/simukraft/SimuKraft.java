package com.resimulators.simukraft;

import com.resimulators.simukraft.client.data.SkinCacher;
import com.resimulators.simukraft.client.gui.GuiMod;
import com.resimulators.simukraft.client.gui.SimHud;
import com.resimulators.simukraft.client.render.MarkerEntityRender;
import com.resimulators.simukraft.common.commands.CommandStructure;
import com.resimulators.simukraft.common.entity.sim.SimInformationOverlay;
import com.resimulators.simukraft.common.events.world.MarkerBrokenEvent;
import com.resimulators.simukraft.common.events.world.NewDayEvent;
import com.resimulators.simukraft.common.events.world.SimDeathEvent;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.*;
import com.resimulators.simukraft.init.ModBlocks;
import com.resimulators.simukraft.init.ModEntities;
import com.resimulators.simukraft.init.ModItems;
import com.resimulators.simukraft.proxy.ClientProxy;
import com.resimulators.simukraft.proxy.IProxy;
import com.resimulators.simukraft.proxy.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MODID)
public class SimuKraft {
    public static final Configs config = new Configs();

    private static final Logger LOGGER = LogManager.getLogger();

    public static final IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public SimuKraft() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec(), "SimUKraft.toml");

        RegistryHandler.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static Logger LOGGER() {
        return LOGGER;
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModEntities.registerAttributes();
        MinecraftForge.EVENT_BUS.register(new NewDayEvent());
        MinecraftForge.EVENT_BUS.register(new FactionEvents());
        MinecraftForge.EVENT_BUS.register(new SimDeathEvent());
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

        ModEntities.registerRenderers();
        ModContainers.registerScreens();
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event){
        CommandStructure.register(event.getDispatcher());
    }
    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        StructureHandler.createTemplateManager(event.getServer());
    }
}

