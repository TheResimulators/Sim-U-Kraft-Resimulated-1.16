package com.Resimulators.simukraft.client.render;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.client.model.EntitySimModel;
import com.Resimulators.simukraft.common.entity.EntitySim;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class EntitySimRender extends LivingRenderer<EntitySim, EntitySimModel> {
    public EntitySimRender(EntityRendererManager manager) {
        super(manager, new EntitySimModel(0.0f), 0.5f);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5f), new BipedModel(1.0f)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public void func_225623_a_(@Nonnull EntitySim entitySim, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        this.setModelVisibilities(entitySim);
        super.func_225623_a_(entitySim, entityYaw, partialTick, matrix, renderer, light);
    }

    private void setModelVisibilities(EntitySim entitySim) {
        EntitySimModel model = this.getEntityModel();
        ItemStack itemStack = entitySim.getHeldItemMainhand();
        ItemStack itemStack1 = entitySim.getHeldItemOffhand();
        model.setVisible(true, true);
        model.field_228270_o_ = entitySim.isCrouching();
        if (entitySim.getPrimaryHand() == HandSide.RIGHT) {
        } else {
        }
    }

    @Override
    public ResourceLocation getEntityTexture(EntitySim entitySim) {
        return new ResourceLocation(Reference.MODID + ":textures/entity/entity_sim.png");
    }



}



