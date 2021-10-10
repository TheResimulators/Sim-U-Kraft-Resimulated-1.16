package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.tileentity.IControlBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;

public class GuiAnimal extends GuiBaseJob {
    IControlBlock farm;
    String name;

    public GuiAnimal(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.ANIMAL_FARMER.getId());
        name = component.getString();
        farm = (IControlBlock) SimuKraft.proxy.getClientWorld().getBlockEntity(pos);
    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(stack, p_render_1_, p_render_2_, p_render_3_);
        drawCenteredString(stack, font, name, width / 2, height / 4 - 20, Color.WHITE.getRGB());
    }
}
