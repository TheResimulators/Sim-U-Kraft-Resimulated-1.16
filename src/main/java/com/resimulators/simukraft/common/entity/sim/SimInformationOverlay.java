package com.resimulators.simukraft.common.entity.sim;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiSimInventory;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.ColorHelper;
import com.resimulators.simukraft.utils.Icons;
import com.resimulators.simukraft.utils.RayTraceHelper;
import com.resimulators.simukraft.utils.Utils;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.CallbackI;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        if (RayTraceHelper.INSTANCE.getTarget() != null && (minecraft.screen == null) || minecraft.screen instanceof GuiSimInventory) {
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
                }
                if (faction != null) {
                    house = faction.getHouseByID(sim.getHouseID());
                } else {
                    house = null;
                    SimuKraft.LOGGER().warn("Sim With UUID " + sim.getUUID() + " Does not belong to a Faction");
                }

                MatrixStack stack = new MatrixStack();
                stack.pushPose();
                saveGLState();

                int guiScale = minecraft.options.guiScale;
                if (guiScale == 0)
                    guiScale = 4;

                float scale = 1;
                int posX = 0;
                int posY = (minecraft.getWindow().getScreenHeight() / (guiScale)) / 2;

                if (minecraft.screen instanceof GuiSimInventory) {
                    posY = ((minecraft.getWindow().getScreenHeight() / (guiScale)) / 2) - ((GuiSimInventory) minecraft.screen).getYSize() / 2;
                }

                stack.scale(scale, scale, 1);

                RenderSystem.disableRescaleNormal();
                RenderHelper.turnOff();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();

                List<Pair<Integer, Pair<RenderPosition, String>>> strings = new ArrayList<>();
                int stringPosY = posY + 48;
                int width = 90;

                //INFO: Add information entries below
                strings.add(createTextPositionPair(new RenderPosition(posX, stringPosY), sim.getFemale() ? "Female" : "Male"));
                strings.add(createTextPositionPair(new RenderPosition(posX, stringPosY += 11), "Single (WIP)"));
                strings.add(createTextPositionPair(new RenderPosition(posX, stringPosY += 11), house != null ? "Lives in " + Utils.uppercaseFirstLetterInEveryWord(house.getName().replace("_", " ")) : "Homeless"));
                strings.add(createTextPositionPair(new RenderPosition(posX, stringPosY += 11), sim.getProfession() != 0 ? Utils.uppercaseFirstLetterInEveryWord(Profession.getNameFromID(sim.getProfession())) : "Unemployed"));

                //Calculate the width of the box.
                for (Pair<Integer, Pair<RenderPosition, String>> string : strings) {
                    int temp = string.getFirst();
                    if (width < temp) {
                        width = temp + 10;
                    }
                }

                if (minecraft.screen instanceof GuiSimInventory) {
                    posX = ((minecraft.getWindow().getScreenWidth() / guiScale) / 2) - ((GuiSimInventory) minecraft.screen).getXSize() / 2 - width;
                }

                //Draw the box
                Rectangle rectangle = new Rectangle(posX, posY, width, stringPosY - posY + 11);
                drawTooltipBox(rectangle.x, rectangle.y, rectangle.width, rectangle.height, new Color(0, 0, 0, 150).getRGB(), ColorHelper.getColorFromDye(sim.getNameColor()).getRGB(), Color.BLACK.getRGB());

                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                //Health Bar
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

                //Armor Bar
                float armor = sim.getArmorValue() / 2f;
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

                //Hunger Bar
                float hunger = sim.foodStats.getFoodLevel() / 2f;
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

                //Draw name and information lines.
                drawLine(stack, new RenderPosition(posX, posY + 5), (SimuKraft.config.getSims().coloredNames.get() ? TextFormatting.getById(ColorHelper.convertDyeToTF(sim.getNameColor())) : TextFormatting.WHITE) + sim.getName().getString());
                for (Pair<Integer, Pair<RenderPosition, String>> pair : strings) {
                    drawLine(stack, posX, pair.getSecond().getFirst().getY(), pair.getSecond().getSecond());
                }

                RenderSystem.disableBlend();
                RenderSystem.enableRescaleNormal();
                loadGLState();
                RenderSystem.enableDepthTest();



                stack.popPose();
            }
        }
        RayTraceHelper.INSTANCE.reset();
    }

    private void drawLine(MatrixStack stack, RenderPosition pos, String s) {
        drawLine(stack, pos.getX(), pos.getY(), s);
    }

    private void drawLine(MatrixStack stack, int x, int y, String s) {
        minecraft.font.draw(stack, s + TextFormatting.RESET, x + 5, y, Color.WHITE.getRGB());
    }

    private Pair<Integer, Pair<RenderPosition, String>> createTextPositionPair(int x, int y, String s) {
        return createTextPositionPair(new RenderPosition(x, y), s);
    }

    private Pair<Integer, Pair<RenderPosition, String>> createTextPositionPair(RenderPosition renderPosition, String s) {
        int width = Minecraft.getInstance().font.width(s);
        return new Pair<>(width, new Pair<>(renderPosition, s));
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

    static class RenderPosition {
        private final int x;
        private final int y;

        public RenderPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
