package com.resimulators.simukraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

public class GuiHandler {
    public static final int Builder = 1;




    public static void openGui(ArrayList<Integer> ints, BlockPos pos, int id,int guiId){
        switch (guiId){
            case Builder:
                Minecraft.getInstance().displayGuiScreen(new BuilderGui(new StringTextComponent("Builder"),ints,pos,id));
                break;
        }
    }
}
