package com.resimulators.simukraft;

import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilSocialInteractionsService;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.resimulators.simukraft.client.data.SkinCacher;
import com.resimulators.simukraft.client.gui.GuiMod;
import com.resimulators.simukraft.client.gui.SimHud;
import com.resimulators.simukraft.client.render.MarkerEntityRender;
import com.resimulators.simukraft.client.render.TileConstructorRenderer;
import com.resimulators.simukraft.common.commands.CommandStructure;
import com.resimulators.simukraft.common.entity.sim.SimInformationOverlay;
import com.resimulators.simukraft.common.events.world.MarkerBrokenEvent;
import com.resimulators.simukraft.common.events.world.NewDayEvent;
import com.resimulators.simukraft.common.events.world.SimDeathEvent;
import com.resimulators.simukraft.common.events.world.SimLoadingUnloadingEvents;
import com.resimulators.simukraft.handlers.ChunkLoadingCallback;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.*;
import com.resimulators.simukraft.proxy.ClientProxy;
import com.resimulators.simukraft.proxy.IProxy;
import com.resimulators.simukraft.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.social.FilterManager;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MODID)
public class SimuKraft {
    public static final Configs config = new Configs();
    public static final IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    private static final Logger LOGGER = LogManager.getLogger();

    private int r = 0, g = 0, b = 0;



    public static Logger LOGGER() {
        return LOGGER;
    }

    public SimuKraft() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec(), "SimUKraft.toml");

        RegistryHandler.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> {
            try {
                doClientStuff(event);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        new StructureInstaller();
        ModEntities.registerAttributes();
        MinecraftForge.EVENT_BUS.register(new NewDayEvent());
        MinecraftForge.EVENT_BUS.register(new FactionEvents());
        MinecraftForge.EVENT_BUS.register(new SimDeathEvent());
        MinecraftForge.EVENT_BUS.register(MarkerBrokenEvent.class);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new SimLoadingUnloadingEvents());
        Network.handler.init();
        event.enqueueWork(() ->
        {
            ForgeChunkManager.setForcedChunkLoadingCallback(Reference.MODID,new ChunkLoadingCallback());
        });

        //StructureHandler.createTemplateManager();
    }

    private void doClientStuff(final FMLClientSetupEvent event) throws IllegalAccessException, NoSuchFieldException {
        // do something that can only be done on the client
        MinecraftForge.EVENT_BUS.register(new SimHud());
        MinecraftForge.EVENT_BUS.register(new SimInformationOverlay());
        MarkerEntityRender.register();
        TileConstructorRenderer.register();
        //Registering SkinCache and Special Skins
        SkinCacher skinCacher = new SkinCacher();
        skinCacher.initSkinService();
        skinCacher.registerSpecialSkins();
        ModGameRules.setupGameRules();
        ModEntities.registerRenderers();
        ModContainers.registerScreens();
        StructureHandler.createTemplateManager();
        //Temporary
        YggdrasilSocialInteractionsService service = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class,Minecraft.getInstance(),"field_244734_au");
        Field field = service.getClass().getDeclaredField("serversAllowed");
        field.setAccessible(true);
        field.set(service,true);
        }

    @SubscribeEvent
    public void onServerStart(FMLServerAboutToStartEvent event){
        if (event.getServer().isDedicatedServer()){
            StructureHandler.createServerTemplateManager();
        }
    }
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandStructure.register(event.getDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
      /*
        drawCube(event, new Vector3d(100, 100, 100), new Vector3d(-100, 150, -100), new Vector3d(r, g, b));
        r = (r + 1) % 256;
        g = (g + 2) % 256;
        b = (b + 3) % 256;
      */
    }

    public static GameRules.RuleType<GameRules.BooleanValue> create(boolean defaultValue) {
        try {
            Method createGameruleMethod = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "func_223568_b", boolean.class);
            createGameruleMethod.setAccessible(true);
            return (GameRules.RuleType<GameRules.BooleanValue>) createGameruleMethod.invoke(null, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }






    @OnlyIn(Dist.CLIENT)
    public static void drawCube(RenderWorldLastEvent event, Vector3d pointA, Vector3d pointB, Vector3d color) {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderType.lines());

        MatrixStack stack = event.getMatrixStack();

        stack.pushPose();

        Vector3d cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        stack.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f mat = stack.last().pose();

        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
        builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

        stack.popPose();
        buffer.endBatch(RenderType.lines());
    }
}

