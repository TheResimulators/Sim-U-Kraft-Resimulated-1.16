package com.resimulators.simukraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

public class GuiHandler {
    public static final int Builder = 1;
    public static final int Miner = 2;
    public static final int GLASS_FACTORY = 3;
    public static final int FARMER = 4;



    public static void openGui(ArrayList<Integer> ints, BlockPos pos, int id,int guiId){
        switch (guiId){
            case Builder:
                Minecraft.getInstance().displayGuiScreen(new BuilderGui(new StringTextComponent("Builder"),ints,pos,id));
                break;
            case Miner:
                Minecraft.getInstance().displayGuiScreen(new MinerGui(new StringTextComponent("Miner"),ints,pos,id));
            case GLASS_FACTORY:
                Minecraft.getInstance().displayGuiScreen(new GlassFactoryGui(new StringTextComponent("Glass Factory"),ints,pos,id));
            case FARMER:
                Minecraft.getInstance().displayGuiScreen(new FarmerGui(new StringTextComponent("Farmer"),ints,pos,id));

        }
    }
}
