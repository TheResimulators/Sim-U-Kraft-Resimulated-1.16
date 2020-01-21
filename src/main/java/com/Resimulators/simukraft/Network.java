package com.Resimulators.simukraft;

import com.Resimulators.simukraft.handlers.SimUKraftPacketHandler;

public class Network {

    public static final SimUKraftPacketHandler handler = new SimUKraftPacketHandler();



    public static SimUKraftPacketHandler getNetwork(){
        return handler;
    }
}
