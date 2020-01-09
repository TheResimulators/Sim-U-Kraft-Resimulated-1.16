package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.client.model.EntitySimModel;
import com.Resimulators.simukraft.client.render.EntitySimRender;
import com.Resimulators.simukraft.common.entity.EntitySim;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.client.registry.RenderingRegistry;


public class ModRenders {
    public static void registerEntityRenders() {

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ENTITY_SIM, EntitySimRender::new);
    }
}
