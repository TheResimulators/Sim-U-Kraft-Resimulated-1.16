package com.resimulators.simukraft.common.entity.sim;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiSimInventory;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.ColorHelper;
import com.resimulators.simukraft.utils.Icons;
import com.resimulators.simukraft.utils.RayTraceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;

public class SimInformationOverlay {
    protected static boolean hasLight;
    protected static boolean hasDepthTest;
    protected static boolean hasLight0;
    protected static boolean hasLight1;
    protected static boolean hasRescaleNormal;
    protected static boolean hasColorMaterial;
    protected static boolean depthMask;
    protected static int depthFunc;
    private final Minecraft minecraft = Minecraft.getInstance();
    private Entity pastEntity;
    private Faction faction;
    private Faction.House house;

    @SubscribeEvent
    public void renderSimOverlay(TickEvent.RenderTickEvent event) {
        RayTraceHelper.INSTANCE.ray();
        if (RayTraceHelper.INSTANCE.getTarget() != null && (minecraft.screen == null || minecraft.screen instanceof ChatScreen) || minecraft.screen instanceof GuiSimInventory) {
            Entity entity = null;
            if (minecraft.screen instanceof GuiSimInventory)
                entity = pastEntity;
            if (entity == null)
                entity = RayTraceHelper.INSTANCE.getTargetEntity();
            pastEntity = entity;

            if (entity instanceof SimEntity) {
                SimEntity sim = (SimEntity) entity;
                if (faction == null) {
                    faction = SavedWorldData.get(sim.getCommandSenderWorld()).getFactionWithSim(sim.getUUID());
                    if (faction != null) {
                        house = faction.getHouseByID(sim.getHouseID());
                    }else{
                        SimuKraft.LOGGER().warn("Sim With UUID " + sim.getUUID() + " Does not belong to a Faction");
                    }
                }
                RenderSystem.pushMatrix();
                saveGLState();

                int guiScale = minecraft.options.guiScale;
                if (guiScale == 0)
                    guiScale = 4;

                float scale = 1;
                int posX = 0;
                int posY = (minecraft.getWindow().getScreenHeight() / (guiScale)) / 2;

                if (minecraft.screen instanceof GuiSimInventory) {
                    posX = ((minecraft.getWindow().getScreenWidth() / guiScale) / 2) - ((GuiSimInventory) minecraft.screen).getXSize() / 2 - 90;
                    posY = ((minecraft.getWindow().getScreenHeight() / (guiScale)) / 2) - ((GuiSimInventory) minecraft.screen).getYSize() / 2;
                    scale = 1;
                }

                RenderSystem.scalef(scale, scale, 1);

                RenderSystem.disableRescaleNormal();
                RenderHelper.turnOff();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                int rectangleHeight = 86;
                if (house != null) {
                    rectangleHeight = 99;

                }
                Rectangle rectangle = new Rectangle(posX, posY, 90, rectangleHeight);
                drawTooltipBox(rectangle.x, rectangle.y, rectangle.width, rectangle.height, new Color(0, 0, 0, 150).getRGB(), ColorHelper.getColorFromDye(sim.getNameColor()).getRGB(), Color.BLACK.getRGB());

                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                float health = sim.getHealth() / 2;
                float maxHealth = sim.getMaxHealth();
                int heartCount = MathHelper.ceil(maxHealth) / 2;
                int heartsPerLine = 10;

                int x = posX + 5;
                int y = posY + 15;
                int xOffset = 0;
                for (int i = 1; i <= heartCount; i++) {
                    if (i <= MathHelper.floor(health)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.HEART);
                        xOffset += 8;
                    }
                    if ((i > health) && (i < health + 1)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.HALF_HEART);
                        xOffset += 8;
                    }
                    if (i >= health + 1) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.EMPTY_HEART);
                        xOffset += 8;
                    }

                    if (i % heartsPerLine == 0) {
                        y += 10;
                        xOffset = 0;
                    }
                }

                float armor = sim.getArmorValue() / 2;
                float maxArmor = 20;
                int armorCount = MathHelper.ceil(maxArmor) / 2;

                xOffset = 0;
                for (int i = 1; i <= armorCount; i++) {
                    if (i <= MathHelper.floor(armor)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.ARMOR);
                        xOffset += 8;
                    }
                    if ((i > armor) && (i < armor + 1)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.HALF_ARMOR);
                        xOffset += 8;
                    }
                    if (i >= armor + 1) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.EMPTY_ARMOR);
                        xOffset += 8;
                    }
                }

                float hunger = sim.foodStats.getFoodLevel() / 2;
                float maxHunger = 20;
                int hungerCount = MathHelper.ceil(maxHunger) / 2;

                y += 10;
                xOffset = 0;
                for (int i = 1; i <= hungerCount; i++) {
                    if (i <= MathHelper.floor(hunger)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.HUNGER);
                        xOffset += 8;
                    }
                    if ((i > hunger) && (i < hunger + 1)) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.HALF_HUNGER);
                        xOffset += 8;
                    }
                    if (i >= hunger + 1) {
                        renderIcon(x + xOffset, y, 8, 8, Icons.EMPTY_HUNGER);
                        xOffset += 8;
                    }
                }

                RenderSystem.disableBlend();
                RenderSystem.enableRescaleNormal();
                loadGLState();
                RenderSystem.enableDepthTest();
                int jobYPos = 76;
                minecraft.font.draw(new MatrixStack(), (SimuKraft.config.getSims().coloredNames.get() ? TextFormatting.getById(ColorHelper.convertDyeToTF(sim.getNameColor())) : TextFormatting.WHITE) + sim.getName().getString(), posX + 5, posY + 5, Color.WHITE.getRGB());

                //TODO: WIP Couple Status (Temp)
                minecraft.font.draw(new MatrixStack(), "Single (WIP)" + TextFormatting.RESET, posX + 5, posY + 50, Color.WHITE.getRGB());


                //TODO:WIP HOUSE STATUS (TEMP)
                if (house != null) {
                    minecraft.font.draw(new MatrixStack(), "Lives in " + TextFormatting.RESET, posX + 5, posY + 63, Color.WHITE.getRGB());
                    minecraft.font.draw(new MatrixStack(), house.getName().replace("_", " ") + TextFormatting.RESET, posX + 5, posY + 76, Color.WHITE.getRGB());
                    jobYPos = 89;
                } else {
                    minecraft.font.draw(new MatrixStack(), "Homeless" + TextFormatting.RESET, posX + 5, posY + 63, Color.WHITE.getRGB());
                }
                if (sim.getJob() == null) {

                    minecraft.font.draw(new MatrixStack(), "Unemployed" + TextFormatting.RESET, posX + 5, posY + jobYPos, Color.WHITE.getRGB());
                } else {
                    String profession = Profession.getNameFromID(sim.getProfession());

                    minecraft.font.draw(new MatrixStack(), StringUtils.capitalize(profession) + TextFormatting.RESET, posX + 5, posY + jobYPos, Color.WHITE.getRGB());

                }
                RenderSystem.popMatrix();
            }
        }
        RayTraceHelper.INSTANCE.reset();
    }

    public static void saveGLState() {
        hasLight = GL11.glGetBoolean(GL11.GL_LIGHTING);
        hasLight0 = GL11.glGetBoolean(GL11.GL_LIGHT0);
        hasLight1 = GL11.glGetBoolean(GL11.GL_LIGHT1);
        hasDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        hasRescaleNormal = GL11.glGetBoolean(GL12.GL_RESCALE_NORMAL);
        hasColorMaterial = GL11.glGetBoolean(GL11.GL_COLOR_MATERIAL);
        depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
    }

    public static void drawTooltipBox(int x, int y, int w, int h, int bg, int grad1, int grad2) {
        drawGradientRect(x + 1, y, w - 1, 1, bg, bg);
        drawGradientRect(x + 1, y + h, w - 1, 1, bg, bg);
        drawGradientRect(x + 1, y + 1, w - 1, h - 1, bg, bg);//center
        drawGradientRect(x, y + 1, 1, h - 1, bg, bg);
        drawGradientRect(x + w, y + 1, 1, h - 1, bg, bg);
        drawGradientRect(x + 1, y + 2, 1, h - 3, grad1, grad2);
        drawGradientRect(x + w - 1, y + 2, 1, h - 3, grad1, grad2);

        drawGradientRect(x + 1, y + 1, w - 1, 1, grad1, grad1);
        drawGradientRect(x + 1, y + h - 1, w - 1, 1, grad2, grad2);
    }

    public static void renderIcon(int x, int y, int sx, int sy, Icons icon) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);

        if (icon == null)
            return;

        if (icon.bu != -1)
            drawTexturedModalRect(x, y, icon.bu, icon.bv, sx, sy, icon.bsu, icon.bsv);
        drawTexturedModalRect(x, y, icon.u, icon.v, sx, sy, icon.su, icon.sv);
    }

    public static void loadGLState() {
        RenderSystem.depthMask(depthMask);
        RenderSystem.depthFunc(depthFunc);
        if (hasLight)
            RenderSystem.enableLighting();
        else
            RenderSystem.disableLighting();
        if (hasLight0)
            GlStateManager._enableLight(0);
        else if (hasLight1)
            GlStateManager._enableLight(1);
        else if (hasDepthTest)
            RenderSystem.enableDepthTest();
        else
            RenderSystem.disableDepthTest();
        if (hasRescaleNormal)
            RenderSystem.enableRescaleNormal();
        else
            RenderSystem.disableRescaleNormal();
        if (hasColorMaterial)
            RenderSystem.enableColorMaterial();
        else
            RenderSystem.disableColorMaterial();
        RenderSystem.popAttributes();
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float zLevel = 0.0F;

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(left + right, top, zLevel).color(f1, f2, f3, f).endVertex();
        buffer.vertex(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        buffer.vertex(left, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        buffer.vertex(left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int tw, int th) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        float zLevel = 0.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(x, y + height, zLevel).uv(((float) (textureX) * f), ((float) (textureY + th) * f1)).endVertex();
        buffer.vertex(x + width, y + height, zLevel).uv(((float) (textureX + tw) * f), ((float) (textureY + th) * f1)).endVertex();
        buffer.vertex(x + width, y, zLevel).uv(((float) (textureX + tw) * f), ((float) (textureY) * f1)).endVertex();
        buffer.vertex(x, y, zLevel).uv(((float) (textureX) * f), ((float) (textureY) * f1)).endVertex();
        tessellator.end();
    }
}
