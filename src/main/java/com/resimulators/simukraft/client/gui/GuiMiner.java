package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;

public class GuiMiner extends GuiBaseJob {


    public GuiMiner(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.MINER.getId());
    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(stack, p_render_1_, p_render_2_, p_render_3_);
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            if (state == State.MAIN) {
                TileMiner tileEntity = (TileMiner) world.getBlockEntity(pos);
                if (tileEntity != null) {
                    if (tileEntity.getMarker() != null) {
                        BlockPos marker = tileEntity.getMarker();
                        font.draw(stack, "Markers Position:", 20, 90, Color.WHITE.getRGB());
                        font.draw(stack, String.format("X: %d, Y: %d, Z: %d", marker.getX(), marker.getY(), marker.getZ()), 20, 110, Color.WHITE.getRGB());
                    }else
                    {
                        font.draw(stack, "Could Not Find Markers, Please place one marker in front block and one in forward direction and one in the right direction", 20, 90, Color.WHITE.getRGB());
                    } {
                        font.draw(stack, new StringTextComponent("Could Not Origin Marker:"), 20, 90, Color.WHITE.getRGB());
                    }
                    font.draw(stack, String.format("Front Left: %s",tileEntity.getMarker() == null ? TextFormatting.RED + "Error" + TextFormatting.RESET : TextFormatting.GREEN + "Found" + TextFormatting.RESET), 200, 90, Color.WHITE.getRGB());
                    font.draw(stack, String.format("Back Left: %s",tileEntity.getDepth() == 0 ? TextFormatting.RED + "Error"  + TextFormatting.RESET: TextFormatting.GREEN + "Found" + TextFormatting.RESET), 200, 110, Color.WHITE.getRGB());
                    font.draw(stack, String.format("Front Right: %s",tileEntity.getWidth() == 0 ? TextFormatting.RED + "Error"  + TextFormatting.RESET: TextFormatting.GREEN +"Found" + TextFormatting.RESET), 200, 130, Color.WHITE.getRGB());
                    font.draw(stack, String.format("Back Right: %s",(tileEntity.getDepth() == 0 || tileEntity.getWidth() == 0) ? TextFormatting.RED + "Error" + TextFormatting.RESET : TextFormatting.GREEN + "Found" + TextFormatting.RESET), 200, 150, Color.WHITE.getRGB());
                    if (tileEntity.getWidth() != 0) {
                        font.draw(stack, "Width: " + tileEntity.getWidth(), width - 80, 90, Color.WHITE.getRGB());
                        font.draw(stack, "Depth: " + tileEntity.getDepth(), width - 80, 120, Color.WHITE.getRGB());
                        font.draw(stack, "Height: " + tileEntity.getYpos(), width - 80, 150, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
    }
}
