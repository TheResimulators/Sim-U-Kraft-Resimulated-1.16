package com.resimulators.simukraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

public class EntitySimModel extends BipedModel<SimEntity> {
    private boolean smallArms;

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
    private SimEntity sim;

    public EntitySimModel(float modelSize) {
        super(modelSize, 0.0F, 64, 64);
        this.smallArms = false;
        this.isChild = false;
        this.femaleArmLeft = new ModelRenderer(this, 32, 48);
        this.femaleArmLeft.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmLeft.setRotationPoint(5.0F, 2.5F, 0.0F);
        this.femaleArmRight = new ModelRenderer(this, 40, 16);
        this.femaleArmRight.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmRight.setRotationPoint(-5.0F, 2.5F, 0.0F);
        this.femaleLeftArmwear = new ModelRenderer(this, 48, 48);
        this.femaleLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
        this.femaleRightArmwear = new ModelRenderer(this, 40, 32);
        this.femaleRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);

        this.maleArmRight = new ModelRenderer(this, 40, 16);
        this.maleArmRight.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmRight.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.maleArmLeft = new ModelRenderer(this, 32, 48);
        this.maleArmLeft.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmLeft.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.maleArmLeft.mirror = true;
        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        setVisible(true,true);
    }

    @Override
    protected Iterable<ModelRenderer> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.bipedLeftLegwear, this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    @Override
    public void setRotationAngles(SimEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);
        this.femaleArmLeft.copyModelAngles(this.bipedLeftArm);
        this.femaleArmRight.copyModelAngles(this.bipedRightArm);
        this.maleArmLeft.copyModelAngles(this.bipedLeftArm);
        this.maleArmRight.copyModelAngles(this.bipedRightArm);
    }

    public void setVisible(boolean visible, boolean female) {
        if (female) {
            this.bipedLeftArm = this.femaleArmLeft;
            this.bipedRightArm = this.femaleArmRight;
            this.femaleArmLeft.showModel = visible;
            this.femaleArmRight.showModel = visible;
            this.bipedLeftArmwear.showModel = !visible;
            this.bipedRightArmwear.showModel = !visible;
            this.femaleLeftArmwear.showModel = visible;
            this.femaleRightArmwear.showModel = visible;
            this.smallArms = true;
        } else {
            this.bipedLeftArm = this.maleArmLeft;
            this.bipedRightArm = this.maleArmRight;
            this.femaleArmLeft.showModel = !visible;
            this.femaleArmRight.showModel = !visible;
            this.femaleLeftArmwear.showModel = !visible;
            this.femaleRightArmwear.showModel = !visible;
            this.bipedLeftArmwear.showModel = visible;
            this.bipedRightArmwear.showModel = visible;
            this.smallArms = false;
        }
        setVisible(visible);
        this.bipedLeftLegwear.showModel = visible;
        this.bipedRightLegwear.showModel = visible;
        this.bipedBodyWear.showModel = visible;
    }

    @Override
    public void translateHand(HandSide side, MatrixStack matrix) {
        ModelRenderer modelrenderer = this.getArmForSide(side);
        if (this.smallArms) {
            float f = 0.5F * (float) (side == HandSide.RIGHT ? 1 : -1);
            modelrenderer.rotationPointX += f;
            modelrenderer.translateRotate(matrix);
            modelrenderer.rotationPointX -= f;
        } else {
          modelrenderer.translateRotate(matrix);
        }
    }
}