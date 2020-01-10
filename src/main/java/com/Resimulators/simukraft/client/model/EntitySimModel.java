package com.Resimulators.simukraft.client.model;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.EntitySim;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.*;

public class EntitySimModel extends BipedModel<EntitySim> {
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
    private EntitySim sim;

    public EntitySimModel(float modelSize) {
        super(modelSize, 0.0F, 64, 64);
        this.smallArms = false;
        this.isChild = false;
        this.femaleArmLeft = new ModelRenderer(this, 32, 48);
        this.femaleArmLeft.func_228301_a_(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmLeft.setRotationPoint(5.0F, 2.5F, 0.0F);
        this.femaleArmRight = new ModelRenderer(this, 40, 16);
        this.femaleArmRight.func_228301_a_(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
        this.femaleArmRight.setRotationPoint(-5.0F, 2.5F, 0.0F);
        this.femaleLeftArmwear = new ModelRenderer(this, 48, 48);
        this.femaleLeftArmwear.func_228301_a_(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
        this.femaleRightArmwear = new ModelRenderer(this, 40, 32);
        this.femaleRightArmwear.func_228301_a_(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
        this.femaleRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);

        this.maleArmRight = new ModelRenderer(this, 40, 16);
        this.maleArmRight.func_228301_a_(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmRight.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.maleArmLeft = new ModelRenderer(this, 32, 48);
        this.maleArmLeft.func_228301_a_(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.maleArmLeft.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.maleArmLeft.mirror = true;
        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.func_228301_a_(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.func_228301_a_(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.func_228301_a_(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.func_228301_a_(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.func_228301_a_(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.func_228301_a_(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        setVisible(true,true);
    }

    @Override
    protected Iterable<ModelRenderer> func_225600_b_() {
        return Iterables.concat(super.func_225600_b_(), ImmutableList.of(this.bipedLeftLegwear, this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    private void copyModelAnglesWithoutPoints(ModelRenderer source, ModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
    }

    @Override
    public void func_225597_a_(EntitySim entitySim, float v, float v1, float v2, float v3, float v4) {
        super.func_225597_a_(entitySim, v, v1, v2, v3, v4);

        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);
    }

    public void setVisible(boolean visible, boolean female) {
        setVisible(visible);
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
        this.bipedLeftLegwear.showModel = visible;
        this.bipedRightLegwear.showModel = visible;
        this.bipedBodyWear.showModel = visible;
    }

    @Override
    public void func_225599_a_(HandSide side, MatrixStack matrix) {
        ModelRenderer modelrenderer = this.getArmForSide(side);
        if (this.smallArms) {
            float f = 0.5F * (float) (side == HandSide.RIGHT ? 1 : -1);
            modelrenderer.rotationPointX += f;
            modelrenderer.func_228307_a_(matrix);
            modelrenderer.rotationPointX -= f;
        } else {
          modelrenderer.func_228307_a_(matrix);
        }
    }


}