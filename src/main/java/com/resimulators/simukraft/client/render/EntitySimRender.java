package com.resimulators.simukraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.data.SkinCacher;
import com.resimulators.simukraft.client.model.EntitySimModel;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.utils.ColorHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class EntitySimRender extends LivingRenderer<SimEntity, EntitySimModel> {
    String DIR = "textures/entity/sim/";

    public EntitySimRender(EntityRendererManager manager) {
        super(manager, new EntitySimModel(0.0f), 0.5f);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<SimEntity>(0.5f), new BipedModel<>(1.0f)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public void render(@Nonnull SimEntity simEntity, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        this.setModelVisibilities(simEntity);
        super.render(simEntity, entityYaw, partialTick, matrix, renderer, light);
    }


    private void setModelVisibilities(SimEntity simEntity) {
        EntitySimModel model = this.getEntityModel();
        model.setVisible(true, simEntity.getFemale());
        model.isSneak = simEntity.isCrouching();
        BipedModel.ArmPose bipedmodel$armpose = this.getArmpose(simEntity, Hand.MAIN_HAND);
        BipedModel.ArmPose bipedmodel$armpose1 = this.getArmpose(simEntity, Hand.OFF_HAND);
        if (bipedmodel$armpose.func_241657_a_())
            bipedmodel$armpose1 = simEntity.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;

        if (simEntity.getPrimaryHand() == HandSide.RIGHT) {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        } else {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        }
    }
    @Override
    protected boolean canRenderName(SimEntity entity){
        return true;
    }

    private BipedModel.ArmPose getArmpose(SimEntity simEntity, Hand hand) {
        ItemStack itemstack = simEntity.getHeldItem(hand);
        if (itemstack.isEmpty()) {
            return BipedModel.ArmPose.EMPTY;
        } else {
            if (simEntity.getActiveHand() == hand && simEntity.getItemInUseCount() > 0) {
                UseAction useaction = itemstack.getUseAction();
                if (useaction == UseAction.BLOCK) {
                    return BipedModel.ArmPose.BLOCK;
                }

                if (useaction == UseAction.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAction.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAction.CROSSBOW && hand == simEntity.getActiveHand()) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!simEntity.isSwingInProgress && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }

    @Override
    public ResourceLocation getEntityTexture(SimEntity simEntity) {
        ResourceLocation location = new ResourceLocation(Reference.MODID, DIR + (simEntity.getFemale() ? "female/" : "male/") + "entity_sim" + simEntity.getVariation() + ".png");
        if (simEntity.getSpecial())
            location = SkinCacher.getSkinForSim(simEntity.getName().getString());
        if (location == null)
            location = new ResourceLocation("textures/entity/steve.png");
        return location;
    }

    @Override
    protected void renderName(SimEntity simEntity, ITextComponent text, MatrixStack matrix, IRenderTypeBuffer renderBuffer, int light) {
        double d = this.renderManager.squareDistanceTo(simEntity);
        matrix.push();
            if (d < 100.0d) {
                matrix.translate(0, (9.0F * 1.2f * 0.025F), 0);


                if (!simEntity.getStatus().equals("")){
                    matrix.scale(.9f,.9f,.9f);
                    super.renderName(simEntity, new StringTextComponent(TextFormatting.YELLOW  + simEntity.getActivity().name + TextFormatting.RESET), matrix, renderBuffer, light);
                    matrix.translate(0, (double) (9.0F *1.2f* 0.025F), 0);
                }
                //matrix.translate(0, (double) (9.0F * 1F * 0.025F), 0);
                matrix.scale(1f,1f,1f);



        }

        super.renderName(simEntity, new StringTextComponent((SimuKraft.config.getSims().coloredNames.get() ? TextFormatting.fromColorIndex(ColorHelper.convertDyeToTF(simEntity.getNameColor())) : TextFormatting.WHITE) + text.getString() + TextFormatting.RESET), matrix, renderBuffer, light);

        matrix.pop();
    }
}



