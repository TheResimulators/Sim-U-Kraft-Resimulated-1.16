package com.Resimulators.simukraft.client.render;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.client.model.EntitySimModel;
import com.Resimulators.simukraft.common.entity.EntitySim;
import com.google.common.base.Preconditions;
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
    String DIR = "textures/entity/sim/";

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
        model.setVisible(true, entitySim.getFemale());
        model.field_228270_o_ = entitySim.isCrouching();
        BipedModel.ArmPose bipedmodel$armpose = this.getArmpose(entitySim, itemStack, itemStack1, Hand.MAIN_HAND);
        BipedModel.ArmPose bipedmodel$armpose1 = this.getArmpose(entitySim, itemStack, itemStack1, Hand.OFF_HAND);
        if (entitySim.getPrimaryHand() == HandSide.RIGHT) {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        } else {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        }
    }

    private BipedModel.ArmPose getArmpose(EntitySim entitySim, ItemStack primary, ItemStack secondary, Hand hand) {
        BipedModel.ArmPose bipedmodel$armpose = BipedModel.ArmPose.EMPTY;
        ItemStack itemStack = hand == Hand.MAIN_HAND ? primary : secondary;
        if (!itemStack.isEmpty()) {
            bipedmodel$armpose = BipedModel.ArmPose.ITEM;
            if (entitySim.getItemInUseCount() > 0) {
                UseAction useAction = itemStack.getUseAction();
                if (useAction == UseAction.BLOCK) {
                    bipedmodel$armpose = BipedModel.ArmPose.BLOCK;
                } else if (useAction == UseAction.BOW) {
                    bipedmodel$armpose = BipedModel.ArmPose.BOW_AND_ARROW;
                } else if (useAction == UseAction.SPEAR) {
                    bipedmodel$armpose = BipedModel.ArmPose.THROW_SPEAR;
                } else if (useAction == UseAction.CROSSBOW && hand == entitySim.getActiveHand()) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else {
                boolean flag = primary.getItem() == Items.CROSSBOW;
                boolean flag1 = CrossbowItem.isCharged(primary);
                boolean flag2 = secondary.getItem() == Items.CROSSBOW;
                boolean flag3 = CrossbowItem.isCharged(secondary);
                if (flag && flag1) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
                if (flag2 && flag3 && primary.getItem().getUseAction(primary) == UseAction.NONE) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return bipedmodel$armpose;
    }

    @Override
    public ResourceLocation getEntityTexture(EntitySim entitySim) {
        return new ResourceLocation(Reference.MODID, DIR + (entitySim.getFemale() ? "female/" : "male/") + "entity_sim" + entitySim.getVariation() + ".png");
    }
}



