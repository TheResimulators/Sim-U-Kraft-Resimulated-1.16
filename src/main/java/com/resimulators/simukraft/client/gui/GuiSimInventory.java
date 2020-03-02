package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.EntitySim;
import com.resimulators.simukraft.common.entity.sim.SimContainer;
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
import net.minecraft.util.text.ITextComponent;

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
    private EntitySim sim;

    public GuiSimInventory(SimContainer container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);
        this.container = container;
        xSize = WIDTH;
        ySize = HEIGHT;
        this.passEvents = true;
    }

    @Override
    protected void init() {
        super.init();
        this.sim = container.getSim();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        this.font.drawString(this.title.getFormattedText(), 80f, 8f, 4210752);
    }

    @Override
    public void render(int x, int y, float z) {
        this.renderBackground();
        super.render(x, y, z);
        this.renderHoveredToolTip(x, y);
        this.oldMouseX = x;
        this.oldMouseY = y;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1,1, 1);
        this.minecraft.getTextureManager().bindTexture(TEXTURE);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.blit(left, top, 0, 0, this.xSize, this.ySize);
        //if (this.sim != null)
            //renderEntity(left + 51, top + 75, 30, (float) (left + 51) - this.oldMouseX, (float) (top + 75 - 50) - this.oldMouseY, this.sim);
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
        Quaternion qx = Vector3f.field_229183_f_.func_229187_a_(180.0F);
        Quaternion qy = Vector3f.field_229179_b_.func_229187_a_(my * 20.0F);
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
        renderManager.func_229089_a_(qy);
        renderManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().func_228019_au_().func_228487_b_();
        renderManager.func_229084_a_(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrix, renderTypeBuffer, 15728880);
        renderTypeBuffer.func_228461_a_();
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
    public void tick() {
        this.ticks++;
        super.tick();
    }
}
