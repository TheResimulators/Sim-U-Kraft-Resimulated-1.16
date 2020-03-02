package com.resimulators.simukraft.init;

import com.resimulators.simukraft.client.render.EntitySimRender;
import net.minecraftforge.fml.client.registry.RenderingRegistry;


public class ModRenders {
    public static void registerEntityRenders() {

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ENTITY_SIM, EntitySimRender::new);
    }
}
