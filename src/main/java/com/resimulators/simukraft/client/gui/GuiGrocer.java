package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

public class GuiGrocer extends Screen {



    protected GuiGrocer(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }



    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack); //Render Background

        font.draw(stack,"WIP",width/2,height/2, Color.WHITE.getRGB());

    }
}
