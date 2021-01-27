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
    public static final int CUSTOM_DATA = 6;
    public static final int BAKER = 7;
    public static final int FISHER_MAN = 8;
    public static final int RESIDENTIAL = 9;



    public static void openGui(ArrayList<Integer> ints, BlockPos pos, int id,int guiId, String string){
        switch (guiId){
            case BUILDER:
                Minecraft.getInstance().displayGuiScreen(new GuiBuilder(new StringTextComponent(string),ints,pos,id));
                break;
            case MINER:
                Minecraft.getInstance().displayGuiScreen(new GuiMiner(new StringTextComponent(string),ints,pos,id));
                break;
            case GLASS_FACTORY:
                Minecraft.getInstance().displayGuiScreen(new GuiGlassFactory(new StringTextComponent(string),ints,pos,id));
                break;
            case FARMER:
                Minecraft.getInstance().displayGuiScreen(new GuiFarmer(new StringTextComponent(string),ints,pos,id));
                break;
            case CUSTOM_DATA:
                Minecraft.getInstance().displayGuiScreen(new GuiCustomData(new StringTextComponent(string),pos));
                break;
            case ANIMAL_FARM:
                Minecraft.getInstance().displayGuiScreen(new GuiAnimal(new StringTextComponent(string),ints,pos,id));
                break;
            case BAKER:
                Minecraft.getInstance().displayGuiScreen(new GuiBaker(new StringTextComponent(string), ints, pos, id));
                break;
            case FISHER_MAN:
                Minecraft.getInstance().displayGuiScreen(new GuiFisher(new StringTextComponent(string), ints,pos,id));
                break;
        }
    }
}
