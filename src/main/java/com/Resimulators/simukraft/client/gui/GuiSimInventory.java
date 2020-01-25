package com.Resimulators.simukraft.client.gui;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.entity.sim.SimContainer;
import com.Resimulators.simukraft.handlers.FoodStats;
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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.Random;
import java.util.UUID;

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
        this.minecraft.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
        this.renderStats();
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

    private void renderStats() {
        EntitySim entitySim = this.sim;
        if (entitySim != null) {
            int i = MathHelper.ceil(entitySim.getHealth());
            boolean flag = this.healthUpdateCounter > (long)this.ticks && (this.healthUpdateCounter - (long)this.ticks) / 3L % 2L == 1L;
            long j = Util.milliTime();
            if (i < this.simHealth && entitySim.hurtResistantTime > 0) {
                this.lastSystemTime = j;
                this.healthUpdateCounter = (long)(this.ticks + 20);
            } else if (i > this.simHealth && entitySim.hurtResistantTime > 0) {
                this.lastSystemTime = j;
                this.healthUpdateCounter = (long)(this.ticks + 10);
            }

            if (j - this.lastSystemTime > 1000L) {
                this.simHealth = i;
                this.lastSimHealth = i;
                this.lastSystemTime = j;
            }

            this.simHealth = i;
            int k = this.lastSimHealth;
            this.rand.setSeed((long)(this.ticks * 312871));
            FoodStats foodstats = entitySim.getFoodStats();
            int l = foodstats.getFoodLevel();
            IAttributeInstance iattributeinstance = entitySim.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
            int i1 = 80;
            int k1 = 18;
            float f = (float)iattributeinstance.getValue();
            int l1 = MathHelper.ceil(entitySim.getAbsorptionAmount());
            int i2 = MathHelper.ceil((f + (float)l1) / 2.0F / 10.0F);
            int j2 = Math.max(10 - (i2 - 2), 3);
            int k2 = k1 - (i2 - 1) * j2 - 10;
            int l2 = k1 - 10;
            int i3 = l1;
            int j3 = entitySim.getTotalArmorValue();
            int k3 = -1;
            if (entitySim.isPotionActive(Effects.REGENERATION)) {
                k3 = this.ticks % MathHelper.ceil(f + 5.0F);
            }

            for(int l3 = 0; l3 < 10; ++l3) {
                if (j3 > 0) {
                    int i4 = i1 + l3 * 8;
                    if (l3 * 2 + 1 < j3) {
                        this.blit(i4, k2, 34, 9, 9, 9);
                    }

                    if (l3 * 2 + 1 == j3) {
                        this.blit(i4, k2, 25, 9, 9, 9);
                    }

                    if (l3 * 2 + 1 > j3) {
                        this.blit(i4, k2, 16, 9, 9, 9);
                    }
                }
            }

            for(int l5 = MathHelper.ceil((f + (float)l1) / 2.0F) - 1; l5 >= 0; --l5) {
                int i6 = 16;
                if (entitySim.isPotionActive(Effects.POISON)) {
                    i6 += 36;
                } else if (entitySim.isPotionActive(Effects.WITHER)) {
                    i6 += 72;
                }

                int j4 = 0;
                if (flag) {
                    j4 = 1;
                }

                int k4 = MathHelper.ceil((float)(l5 + 1) / 10.0F) - 1;
                int l4 = i1 + l5 % 10 * 8;
                int i5 = k1 - k4 * j2;
                if (i <= 4) {
                    i5 += this.rand.nextInt(2);
                }

                if (i3 <= 0 && l5 == k3) {
                    i5 -= 2;
                }

                int j5 = 0;

                this.blit(l4, i5, 16 + j4 * 9, 9 * j5, 9, 9);
                if (flag) {
                    if (l5 * 2 + 1 < k) {
                        this.blit(l4, i5, i6 + 54, 9 * j5, 9, 9);
                    }

                    if (l5 * 2 + 1 == k) {
                        this.blit(l4, i5, i6 + 63, 9 * j5, 9, 9);
                    }
                }

                if (i3 > 0) {
                    if (i3 == l1 && l1 % 2 == 1) {
                        this.blit(l4, i5, i6 + 153, 9 * j5, 9, 9);
                        --i3;
                    } else {
                        this.blit(l4, i5, i6 + 144, 9 * j5, 9, 9);
                        i3 -= 2;
                    }
                } else {
                    if (l5 * 2 + 1 < i) {
                        this.blit(l4, i5, i6 + 36, 9 * j5, 9, 9);
                    }

                    if (l5 * 2 + 1 == i) {
                        this.blit(l4, i5, i6 + 45, 9 * j5, 9, 9);
                    }
                }
            }

            for(int k6 = 0; k6 < 10; ++k6) {
                int i7 = k1 + 10;
                int k7 = 16;
                int i8 = 0;
                if (entitySim.isPotionActive(Effects.HUNGER)) {
                    k7 += 36;
                    i8 = 13;
                }

                if (entitySim.getFoodStats().getSaturationLevel() <= 0.0F && this.ticks % (l * 3 + 1) == 0) {
                    i7 = k1 + (this.rand.nextInt(3) - 1);
                }

                int k8 = i1 + k6 * 8;
                this.blit(k8, i7, 16 + i8 * 9, 27, 9, 9);
                if (k6 * 2 + 1 < l) {
                    this.blit(k8, i7, k7 + 36, 27, 9, 9);
                }

                if (k6 * 2 + 1 == l) {
                    this.blit(k8, i7, k7 + 45, 27, 9, 9);
                }
            }
        }
    }

    @Override
    public void tick() {
        this.ticks++;
        super.tick();
    }
}
