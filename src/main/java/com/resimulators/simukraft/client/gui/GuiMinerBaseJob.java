package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.common.jobs.Profession;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;

public class GuiMinerBaseJob extends GuiBaseJob {


    public GuiMinerBaseJob(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.MINER.getId());
    }

    @Override
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);

    }

    @Override
    public void func_230430_a_(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.func_230430_a_(stack, p_render_1_, p_render_2_, p_render_3_);
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            if (state == State.MAIN) {
            TileMiner tileEntity = (TileMiner)world.getTileEntity(pos);
            if (tileEntity !=null) {
                if (tileEntity.getMarker() != null) {
                    BlockPos marker = tileEntity.getMarker();
                        field_230712_o_.func_238421_b_(stack, "Markers Position:", 20, 90, Color.WHITE.getRGB());
                        field_230712_o_.func_238421_b_(stack, String.format("X: %d, Y: %d, Z: %d", marker.getX(), marker.getY(), marker.getZ()), 20, 110, Color.WHITE.getRGB());
                    }
                if(tileEntity.getWidth() != 0){
                    field_230712_o_.func_238421_b_(stack, "Width: " + tileEntity.getWidth(), field_230708_k_-80, 90, Color.WHITE.getRGB());
                    field_230712_o_.func_238421_b_(stack, "Depth: " + tileEntity.getDepth(), field_230708_k_-80, 120, Color.WHITE.getRGB());
                    field_230712_o_.func_238421_b_(stack, "Height: " + tileEntity.getYpos(), field_230708_k_-80, 150, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
    }
}
