package com.Resimulators.simukraft.handlers;

import com.Resimulators.simukraft.Reference;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class SimUKraftPacketHandler {

    private static int ID = 0;

    private int newId(){
        return ID;
    }


    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );



    public static void init(){

    }



}
