package com.resimulators.simukraft.client.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class GuiSimInventory extends DisplayEffectsScreen<SimContainer> {
    public static final int WIDTH = 176;
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final int HEIGHT = 224;
    protected final Random rand = new Random();
    private final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/sim_inventory.png");
    private final SimContainer container;
    protected int ticks;
    protected int simHealth;
    protected int lastSimHealth;
    protected long lastSystemTime;
    protected long healthUpdateCounter;
    protected int scaledWidth;
    protected int scaledHeight;
    private float oldMouseX;
    private float oldMouseY;
    private SimEntity sim;

    public GuiSimInventory(SimContainer container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);
        this.container = container;
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        this.passEvents = true;
    }

    @Override
    protected void init() {
        super.init();
        this.sim = container.getSim();
        this.sim.setCustomName(new StringTextComponent(this.title.getString()));


    }

    @Override
    public void render(MatrixStack stack, int x, int y, float z) {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
        this.oldMouseX = x;
        this.oldMouseY = y;
    }

    @Override
    protected void renderLabels(MatrixStack stack, int x, int y) {


        this.font.draw(stack, this.title.getString(), 80f, 8f, 4210752);
        this.font.draw(stack, "Job: " + StringUtils.capitalize(container.job), 80f, 30f, 4210752);
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(TEXTURE);
        int left = this.leftPos;
        int top = this.topPos;
        this.blit(stack, left, top, 0, 0, this.imageWidth, this.imageHeight);
        if (this.sim != null)
            renderEntity(left + 51, top + 75, 30, (float) (left + 51) - this.oldMouseX, (float) (top + 75 - 50) - this.oldMouseY, this.sim);
    }

    public static void renderEntity(int x, int y, int z, float mouseX, float mouseY, LivingEntity entity) {
        float mx = (float) Math.atan(mouseX / 40.0F);
        float my = (float) Math.atan(mouseY / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrix = new MatrixStack();
        matrix.translate(0.0D, 0.0D, 1000.0D);
        matrix.scale((float) z, (float) z, (float) z);
        Quaternion qx = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion qy = Vector3f.XP.rotationDegrees(my * 20.0F);
        qx.mul(qy);
        matrix.mulPose(qx);
        float yawOffset = entity.yBodyRot;
        float rotYaw = entity.yRot;
        float rotPitch = entity.xRot;
        float prevRotYawHead = entity.yHeadRotO;
        float rotYawHead = entity.yHeadRot;
        entity.yBodyRot = 180.0F + mx * 20.0F;
        entity.yRot = 180.0F + mx * 40.0F;
        entity.xRot = -my * 20.0F;
        entity.yHeadRot = entity.yRot;
        entity.yHeadRotO = entity.yRot;
        EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        qy.conj();
        renderManager.overrideCameraOrientation(qy);
        renderManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> renderManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrix, renderTypeBuffer, 15728880));
        renderTypeBuffer.endBatch();
        renderManager.setRenderShadow(true);
        entity.yBodyRot = yawOffset;
        entity.yRot = rotYaw;
        entity.xRot = rotPitch;
        entity.yHeadRotO = prevRotYawHead;
        entity.yHeadRot = rotYawHead;
        RenderSystem.popMatrix();
    }

    @Override
    public void tick() {
        this.ticks++;
        super.tick();
    }

    private void renderIcon(int vertexX, int vertexY, ItemStack stack, int intU, int intV) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            IBakedModel iBakedModel = minecraft.getItemRenderer().getItemModelShaper().getItemModel(stack);
            TextureAtlasSprite textureAtlasSprite = iBakedModel.getParticleIcon();
            minecraft.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.vertex((vertexX), vertexY + intV, 0.0D).uv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1()).endVertex();
            buffer.vertex(vertexX + intU, vertexY + intV, 0.0D).uv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1()).endVertex();
            buffer.vertex(vertexX + intU, (vertexY), 0.0D).uv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0()).endVertex();
            buffer.vertex((vertexX), (vertexY), 0.0D).uv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0()).endVertex();
            tessellator.end();
        } catch (Exception e) {
        }
    }
}
