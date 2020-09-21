package com.resimulators.simukraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

public class GuiHandler {
    public static final int BUILDER = 1;
    public static final int MINER = 2;
    public static final int GLASS_FACTORY = 3;
    public static final int FARMER = 4;
    public static final int ANIMAL_FARM = 5;
    public static final int COW_FARM = 6;
    public static final int SHEEP_FARM = 7;
    public static final int CUSTOM_DATA = 8;
    public static final int BAKER = 9;



    public static void openGui(ArrayList<Integer> ints, BlockPos pos, int id,int guiId){
        switch (guiId){
            case BUILDER:
                Minecraft.getInstance().displayGuiScreen(new GuiBuilder(new StringTextComponent("Builder"),ints,pos,id));
                break;
            case MINER:
                Minecraft.getInstance().displayGuiScreen(new GuiMinerBaseJob(new StringTextComponent("Miner"),ints,pos,id));
                break;
            case GLASS_FACTORY:
                Minecraft.getInstance().displayGuiScreen(new GuiGlassFactory(new StringTextComponent("Glass Factory"),ints,pos,id));
                break;
            case FARMER:
                Minecraft.getInstance().displayGuiScreen(new GuiFarmer(new StringTextComponent("Farmer"),ints,pos,id));
                break;

        }
    }
}
