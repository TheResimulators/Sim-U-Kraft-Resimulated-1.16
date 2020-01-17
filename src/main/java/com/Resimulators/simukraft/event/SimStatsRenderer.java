package com.Resimulators.simukraft.event;

import com.Resimulators.simukraft.SimuKraft;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class SimStatsRenderer {
    List<EntitySim> sims = new ArrayList<>();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity renderEntity = minecraft.getRenderViewEntity();
        BlockPos position = renderEntity.getPosition();

        float partialTicks = event.getPartialTicks();

        Entity focused = getEntityLookedAt(minecraft.player);
        if (focused != null && focused instanceof EntitySim && focused.isAlive()) {
            renderSimStats((EntitySim)focused, partialTicks, renderEntity);
        }
    }

    public void renderSimStats(EntitySim sim, float partialTicks, Entity viewPoint) {
        Stack<LivingEntity> ridingStack = new Stack<>();

        LivingEntity entity = sim;
        ridingStack.push(entity);

        while(entity.getRidingEntity() != null && entity.getRidingEntity() instanceof LivingEntity) {
            entity = (LivingEntity) entity.getRidingEntity();
            ridingStack.push(entity);
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;

        float pastTranslate = 0f;
        while(!ridingStack.isEmpty()) {
            entity = ridingStack.pop();


            processing: {
                float distance = sim.getDistance(viewPoint);
                if (distance > 24)
                    break processing;

                double x = sim.lastTickPosX + (sim.getPosition().getX() - sim.lastTickPosX) * partialTicks;
                double y = sim.lastTickPosY + (sim.getPosition().getY() - sim.lastTickPosY) * partialTicks;
                double z = sim.lastTickPosZ + (sim.getPosition().getZ() - sim.lastTickPosZ) * partialTicks;

                float scale = 0.03f;
                float maxHealth = entity.getMaxHealth();
                float health = Math.min(maxHealth, entity.getHealth());

                if (maxHealth <= 0)
                    break processing;

                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
                Vec3d renderView = minecraft.gameRenderer.getActiveRenderInfo().getProjectedView();
                BlockPos renderPos = renderManager.info.getRenderViewEntity().getPosition();
                double renderPosX = renderPos.getX();
                double renderPosY = renderPos.getY();
                double renderPosZ = renderPos.getZ();

                SimuKraft.LOGGER().debug("Rendering Overlay at X: {}, Y: {}, Z: {}", (float)(x - renderPosX), (float)(y - renderPosY + sim.getHeight() - 0.2f), (float)(z - renderPosZ));

                GlStateManager.func_227626_N_(); //Push Matrix
                GlStateManager.func_227688_c_((float)(x - renderPosX), (float)(y - renderPosY + sim.getHeight() - 0.2f), (float)(z - renderPosZ)); //Translate
                GL11.glNormal3f(0.0f, 1.0f, 0.0f);
                GlStateManager.func_227689_c_((float)-renderView.y, 0, 1, 0); //Rotate
                GlStateManager.func_227689_c_((float)renderView.x, 1, 0, 0); //Rotate
                GlStateManager.func_227672_b_(-scale, -scale, scale); //Scale
                boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
                GlStateManager.func_227722_g_(); //Disable Lighting
                GlStateManager.func_227667_a_(false); //Set Depth Mask
                GlStateManager.func_227621_I_(); //Disable Texture
                GlStateManager.func_227740_m_(); //Enable Blend
                GlStateManager.func_227676_b_(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                float padding = 2;
                int backgroundHeight = 6;
                int infoHeight = 4;
                float size = 25;

                ItemStack stack = null;
                int armor = entity.getTotalArmorValue();
                GlStateManager.func_227688_c_(0, pastTranslate, 0); //Translate

                float s = 0.5f;
                String name = I18n.format(entity.getDisplayName().getFormattedText());
                if (entity.hasCustomName())
                    name = TextFormatting.ITALIC + entity.getCustomName().getFormattedText();

                float name1 = minecraft.fontRenderer.getStringWidth(name) * s;
                if (name1 + 20 < size * 2)
                    size = name1 / 2f + 10f;
                float healthSize = size * (health / maxHealth);

                //Background
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.func_225582_a_(-size - padding, -backgroundHeight, 0.0D).func_225586_a_(0, 0, 0, 64).endVertex();
                buffer.func_225582_a_(-size - padding, infoHeight + padding, 0.0D).func_225586_a_(0, 0, 0, 64).endVertex();
                buffer.func_225582_a_(size + padding, infoHeight + padding, 0.0D).func_225586_a_(0, 0, 0, 64).endVertex();
                buffer.func_225582_a_(size + padding, -backgroundHeight, 0.0D).func_225586_a_(0, 0, 0, 64).endVertex();
                tessellator.draw();

                GlStateManager.func_227619_H_(); //Enable Texture

                GlStateManager.func_227626_N_(); //Push Matrix
                GlStateManager.func_227688_c_(-size, -4.5f, 0); //Translate
                GlStateManager.func_227672_b_(s, s, s); //Scale
                minecraft.fontRenderer.drawString(name, 0, 0, 0xFFFFFF);

                GlStateManager.func_227626_N_(); //Push Matrix
                float s1 = 0.75f;
                GlStateManager.func_227672_b_(s1, s1, s1); //Scale

                int h = 14;
                String maxHp = TextFormatting.BOLD + "" + (int)maxHealth;
                String hp = "" + (int)health;

                minecraft.fontRenderer.drawString(hp, 2, h, 0xFFFFFF);
                minecraft.fontRenderer.drawString(maxHp, (int)(size / (s * s1) * 2) - 2 - minecraft.fontRenderer.getStringWidth(maxHp), h, 0xFFFFFF);

                GlStateManager.func_227627_O_(); //Pop Matrix

                GlStateManager.func_227673_b_(1.0f, 1.0f, 1.0f, 1.0f); //Color4f
                int off = 0;

                s1 = 0.5f;
                GlStateManager.func_227672_b_(s1, s1, s1); //Scale
                GlStateManager.func_227688_c_(size / (s * s1) * 2 - 16, 0, 0); //Translate
                minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                if (stack != null) {
                    renderIcon(off, 0, stack, 16, 16);
                    off -= 16;
                }

                if (armor > 0) {
                    int ironArmor = armor % 5;
                    int diamondArmor = armor / 5;

                    stack = new ItemStack(Items.IRON_CHESTPLATE);
                    for (int i = 0; i < ironArmor; i++) {
                        renderIcon(off, 0, stack, 16, 16);
                        off -= 4;
                    }

                    stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                    for (int i = 0; i < diamondArmor; i++) {
                        renderIcon(off, 0, stack, 16, 16);
                        off -= 4;
                    }
                }

                GlStateManager.func_227627_O_(); //Pop Matrix

                GlStateManager.func_227737_l_(); //Disable Blend
                GlStateManager.func_227667_a_(true); //Set Depth Mask
                if (lighting)
                    GlStateManager.func_227716_f_(); //Enable Lighting
                GlStateManager.func_227673_b_(1.0f, 1.0f, 1.0f, 1.0f); //Color4f
                GlStateManager.func_227627_O_(); //Pop Matrix

                pastTranslate -= backgroundHeight + infoHeight + padding;

                SimuKraft.LOGGER().debug("Reached end of renderer.");
            }
        }
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
            buffer.func_225582_a_((vertexX), vertexY + intV, 0.0D).func_225583_a_(textureAtlasSprite.getMinU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.func_225582_a_(vertexX + intU, vertexY + intV, 0.0D).func_225583_a_(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.func_225582_a_(vertexX + intU, (vertexY), 0.0D).func_225583_a_(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMinV()).endVertex();
            buffer.func_225582_a_((vertexX), (vertexY), 0.0D).func_225583_a_(textureAtlasSprite.getMinU(), textureAtlasSprite.getMinV()).endVertex();
            tessellator.draw();
        } catch (Exception e) {}
    }

    public static Entity getEntityLookedAt(Entity e) {
        Entity foundEntity = null;

        final double finalDistance = 32;
        double distance = finalDistance;
        RayTraceResult pos = raycast(e, finalDistance);

        Vec3d positionVector = e.getPositionVector();
        if(e instanceof PlayerEntity)
            positionVector = positionVector.add(0, e.getEyeHeight(), 0);

        if(pos != null)
            distance = pos.getHitVec().distanceTo(positionVector);

        Vec3d lookVector = e.getLookVec();
        Vec3d reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e.getBoundingBox().grow(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expand(1F, 1F, 1F));
        double minDistance = distance;

        for(Entity entity : entitiesInBoundingBox) {
            if(entity.canBeCollidedWith()) {
                float collisionBorderSize = entity.getCollisionBorderSize();
                AxisAlignedBB hitbox = entity.getBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                Optional<Vec3d> interceptPosition = hitbox.rayTrace(positionVector, reachVector);
                Vec3d interceptVec = interceptPosition.orElse(null);

                if(hitbox.contains(positionVector)) {
                    if(0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if(interceptVec != null) {
                    double distanceToEntity = positionVector.distanceTo(interceptVec);

                    if(distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if(lookedEntity != null && (minDistance < distance || pos == null))
                foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    public static RayTraceResult raycast(Entity e, double len) {
        Vec3d vec = new Vec3d(e.getPosition().getX(), e.getPosition().getY(), e.getPosition().getZ());
        if(e instanceof PlayerEntity)
            vec = vec.add(new Vec3d(0, e.getEyeHeight(), 0));

        Vec3d look = e.getLookVec();
        if(look == null)
            return null;

        return raycast(e.getEntityWorld(), vec, look, e, len);
    }

    public static RayTraceResult raycast(World world, Vec3d origin, Vec3d ray, Entity e, double len) {
        Vec3d end = origin.add(ray.normalize().scale(len));
        RayTraceResult pos = world.rayTraceBlocks(new RayTraceContext(origin, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, e));
        return pos;
    }
}
