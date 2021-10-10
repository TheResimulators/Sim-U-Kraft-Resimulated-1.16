package com.resimulators.simukraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

public class EntitySimModel extends BipedModel<SimEntity> {
    public ModelRenderer bipedLeftArmwear;
    public ModelRenderer bipedRightArmwear;
    public ModelRenderer bipedLeftLegwear;
    public ModelRenderer bipedRightLegwear;
    public ModelRenderer bipedBodyWear;
    public ModelRenderer femaleLeftArmwear;
    public ModelRenderer femaleRightArmwear;
    public ModelRenderer femaleArmLeft;
    public ModelRenderer femaleArmRight;
    public ModelRenderer maleArmLeft;
    public ModelRenderer maleArmRight;
    private boolean smallArms;
    private SimEntity sim;

    public EntitySimModel(float modelSize) {
        super(modelSize, 0.0F, 64, 64);
        this.smallArms = false;
        this.young = false;
        this.femaleArmLeft = new ModelRenderer(this, 32, 48);
        this.femaleArmLeft.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmLeft.setPos(5.0F, 2.5F, 0.0F);
        this.femaleArmRight = new ModelRenderer(this, 40, 16);
        this.femaleArmRight.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmRight.setPos(-5.0F, 2.5F, 0.0F);
        this.femaleLeftArmwear = new ModelRenderer(this, 48, 48);
        this.femaleLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleLeftArmwear.setPos(5.0F, 2.5F, 0.0F);
        this.femaleRightArmwear = new ModelRenderer(this, 40, 32);
        this.femaleRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleRightArmwear.setPos(-5.0F, 2.5F, 10.0F);

        this.maleArmRight = new ModelRenderer(this, 40, 16);
        this.maleArmRight.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmRight.setPos(-5.0F, 2.0F, 0.0F);
        this.maleArmLeft = new ModelRenderer(this, 32, 48);
        this.maleArmLeft.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmLeft.setPos(5.0F, 2.0F, 0.0F);
        this.maleArmLeft.mirror = true;
        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmwear.setPos(5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmwear.setPos(-5.0F, 2.0F, 10.0F);

        this.leftLeg = new ModelRenderer(this, 16, 48);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegwear.setPos(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegwear.setPos(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyWear.setPos(0.0F, 0.0F, 0.0F);
        setVisible(true, true);
    }

    public void setVisible(boolean visible, boolean female) {
        if (female) {
            this.leftArm = this.femaleArmLeft;
            this.rightArm = this.femaleArmRight;
            this.femaleArmLeft.visible = visible;
            this.femaleArmRight.visible = visible;
            this.bipedLeftArmwear.visible = !visible;
            this.bipedRightArmwear.visible = !visible;
            this.femaleLeftArmwear.visible = visible;
            this.femaleRightArmwear.visible = visible;
            this.smallArms = true;
        } else {
            this.leftArm = this.maleArmLeft;
            this.rightArm = this.maleArmRight;
            this.femaleArmLeft.visible = !visible;
            this.femaleArmRight.visible = !visible;
            this.femaleLeftArmwear.visible = !visible;
            this.femaleRightArmwear.visible = !visible;
            this.bipedLeftArmwear.visible = visible;
            this.bipedRightArmwear.visible = visible;
            this.smallArms = false;
        }
        setAllVisible(visible);
        this.bipedLeftLegwear.visible = visible;
        this.bipedRightLegwear.visible = visible;
        this.bipedBodyWear.visible = visible;
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.bipedLeftLegwear, this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    @Override
    public void setupAnim(SimEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyFrom(this.leftLeg);
        this.bipedRightLegwear.copyFrom(this.rightLeg);
        this.bipedLeftArmwear.copyFrom(this.leftArm);
        this.bipedRightArmwear.copyFrom(this.rightArm);
        this.bipedBodyWear.copyFrom(this.body);
        this.femaleArmLeft.copyFrom(this.leftArm);
        this.femaleArmRight.copyFrom(this.rightArm);
        this.maleArmLeft.copyFrom(this.leftArm);
        this.maleArmRight.copyFrom(this.rightArm);
    }

    @Override
    public void translateToHand(HandSide side, MatrixStack matrix) {
        ModelRenderer modelrenderer = this.getArm(side);
        if (this.smallArms) {
            float f = 0.5F * (float) (side == HandSide.RIGHT ? 1 : -1);
            modelrenderer.x += f;
            modelrenderer.translateAndRotate(matrix);
            modelrenderer.x -= f;
        } else {
            modelrenderer.translateAndRotate(matrix);
        }
    }
}