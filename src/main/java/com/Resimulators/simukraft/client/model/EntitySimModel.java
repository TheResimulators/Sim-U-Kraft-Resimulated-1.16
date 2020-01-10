package com.Resimulators.simukraft.client.model;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.EntitySim;
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
    private static final ResourceLocation ENTITY_TEXTURE = new ResourceLocation(Reference.MODID+":textures/entity/entity_sim.png");
    private final RenderType RENDER_TYPE = func_228282_a_(ENTITY_TEXTURE);
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
    @Override
    public void func_225597_a_(EntitySim entitySim, float v, float v1, float v2, float v3, float v4) {
        sim = entitySim;

    }

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
        this.bipedBodyWear.func_228301_a_(4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        setVisible(true,true);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        Color color = Color.pink;
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(180));
        IVertexBuilder vertexBuilder = renderer.getBuffer(RENDER_TYPE);
        func_225598_a_(matrix, vertexBuilder, light, OverlayTexture.field_229196_a_, color.getRed(), color.getGreen(), color.getBlue(), 1);
        matrix.func_227865_b_();
    }
    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        super.func_225598_a_(matrix,vertexBuilder,light,overlayLight,red,green,blue,alpha);
        matrix.func_227860_a_();
        matrix.func_227862_a_(1.5F, 1.5F, 1.5F);
        matrix.func_227861_a_(0, -0.07, 0);
        GlStateManager.func_227626_N_();

        if (this.isChild) {
            GlStateManager.func_227672_b_(0.5F, 0.5F, 0.5F);

           // GlStateManager.func_227688_c_(0.0F, 24.0F * scale, 0.0F);
            this.femaleArmLeft.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleArmRight.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedLeftLegwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedRightLegwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedLeftArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedRightArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleLeftArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleRightArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedBodyWear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        } else {
            //if (sim.isCrouching()) {
               // GlStateManager.func_227688_c_(0.0F, 0.2F, 0.0F);
            //}
            this.femaleArmLeft.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleArmRight.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedLeftLegwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedRightLegwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedLeftArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedRightArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleLeftArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.femaleRightArmwear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            this.bipedBodyWear.func_228309_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        }
        matrix.func_227860_a_();
        matrix.func_227862_a_(0.2F, 1, 0.2F);
        matrix.func_227865_b_();
        //String.func_228309_a_(matrix, vertexBuilder, light, overlayLight, 1, 1, 1, 1);
        matrix.func_227865_b_();
        GlStateManager.func_227627_O_();
    }

    private void copyModelAnglesWithoutPoints(ModelRenderer source, ModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
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

    public void postRenderArm(float scale, HandSide side) {
        ModelRenderer modelrenderer = this.getArmForSide(side);
        if (this.smallArms) {
            float f = 0.5F * (float) (side == HandSide.RIGHT ? 1 : -1);
            modelrenderer.rotationPointX += f;
            //modelrenderer.postRender(scale);
            modelrenderer.rotationPointX -= f;
        } else {
          //  modelrenderer.postRender(scale);
        }
    }


}