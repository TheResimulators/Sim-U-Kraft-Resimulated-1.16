package com.Resimulators.simukraft.client.render;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.client.model.EntitySimModel;
import com.Resimulators.simukraft.common.entity.EntitySim;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;
import java.awt.*;

public class EntitySimRender extends EntityRenderer<EntitySim> {


    private static final EntitySimModel model = new EntitySimModel(2);

    public EntitySimRender(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(EntitySim entity) {
        return new ResourceLocation(Reference.MODID + ":textures/entity/entity_sim.png");
    }


    @Override
    public void func_225623_a_(@Nonnull EntitySim entitySim, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.func_227860_a_();
        model.render(matrix, renderer,light);
        matrix.func_227865_b_();
        super.func_225623_a_(entitySim, entityYaw, partialTick, matrix, renderer, light);

    }


    public static class RenderFactory implements IRenderFactory<EntitySim> {


        @Override
        public EntityRenderer<? super EntitySim> createRenderFor(EntityRendererManager manager) {
            return new EntitySimRender(manager);
        }
    }
}



