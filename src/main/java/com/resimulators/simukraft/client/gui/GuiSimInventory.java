package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.*;
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

import java.util.Random;

public class GuiSimInventory extends DisplayEffectsScreen<SimContainer> {
    private static final int HEIGHT = 224;
    public static final int WIDTH = 176;
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/sim_inventory.png");
    protected final Random rand = new Random();

    private float oldMouseX;
    private float oldMouseY;

    protected int ticks;
    protected int simHealth;
    protected int lastSimHealth;
    protected long lastSystemTime;
    protected long healthUpdateCounter;
    protected int scaledWidth;
    protected int scaledHeight;

    private SimContainer container;
    private SimEntity sim;

    public GuiSimInventory(SimContainer container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);
        this.container = container;
        xSize = WIDTH;
        ySize = HEIGHT;
        this.field_230711_n_ = true;
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        this.sim = container.getSim();
        this.sim.setCustomName(new StringTextComponent(this.field_230704_d_.getString()));
    }

    @Override
    protected void func_230451_b_(MatrixStack stack, int x, int y) {
        this.field_230712_o_.func_238421_b_(stack, this.field_230704_d_.getString(), 80f, 8f, 4210752);
    }

    @Override
    public void func_230430_a_(MatrixStack stack, int x, int y, float z) {
        this.func_230446_a_(stack);
        super.func_230430_a_(stack, x, y, z);
        this.func_230459_a_(stack, x, y);
        this.oldMouseX = x;
        this.oldMouseY = y;
    }

    @Override
    protected void func_230450_a_(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1,1, 1);
        this.field_230706_i_.getTextureManager().bindTexture(TEXTURE);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.func_238474_b_(stack, left, top, 0, 0, this.xSize, this.ySize);
        //SimuKraft.LOGGER().debug(this.sim.getName().getString());
        if (this.sim != null)
            renderEntity(left + 51, top + 75, 30, (float) (left + 51) - this.oldMouseX, (float) (top + 75 - 50) - this.oldMouseY, this.sim);
    }

    public static void renderEntity(int x, int y, int z, float mouseX, float mouseY, LivingEntity entity) {
        float mx = (float)Math.atan((double)(mouseX / 40.0F));
        float my = (float)Math.atan((double)(mouseY / 40.0F));
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)x, (float)y, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrix = new MatrixStack();
        matrix.translate(0.0D, 0.0D, 1000.0D);
        matrix.scale((float)z, (float)z, (float)z);
        Quaternion qx = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion qy = Vector3f.XP.rotationDegrees(my * 20.0F);
        qx.multiply(qy);
        matrix.rotate(qx);
        float yawOffset = entity.renderYawOffset;
        float rotYaw = entity.rotationYaw;
        float rotPitch = entity.rotationPitch;
        float prevRotYawHead = entity.prevRotationYawHead;
        float rotYawHead = entity.rotationYawHead;
        entity.renderYawOffset = 180.0F + mx * 20.0F;
        entity.rotationYaw = 180.0F + mx * 40.0F;
        entity.rotationPitch = -my * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        qy.conjugate();
        renderManager.setCameraOrientation(qy);
        renderManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> renderManager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrix, renderTypeBuffer, 15728880));
        renderTypeBuffer.finish();
        renderManager.setRenderShadow(true);
        entity.renderYawOffset = yawOffset;
        entity.rotationYaw = rotYaw;
        entity.rotationPitch = rotPitch;
        entity.prevRotationYawHead = prevRotYawHead;
        entity.rotationYawHead = rotYawHead;
        RenderSystem.popMatrix();
    }

    private void renderIcon(int vertexX, int vertexY, ItemStack stack, int intU, int intV) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            IBakedModel iBakedModel = minecraft.getItemRenderer().getItemModelMesher().getItemModel(stack);
            TextureAtlasSprite textureAtlasSprite = iBakedModel.getParticleTexture();
            minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos((vertexX), vertexY + intV, 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.pos(vertexX + intU, vertexY + intV, 0.0D).tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.pos(vertexX + intU, (vertexY), 0.0D).tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMinV()).endVertex();
            buffer.pos((vertexX), (vertexY), 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMinV()).endVertex();
            tessellator.draw();
        } catch (Exception e) {}
    }

    @Override
    public void func_231023_e_() {
        this.ticks++;
        super.func_231023_e_();
    }
}
