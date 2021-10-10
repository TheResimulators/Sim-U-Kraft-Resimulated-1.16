package com.resimulators.simukraft;

import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;

public class Network {

    public static final SimUKraftPacketHandler handler = new SimUKraftPacketHandler();


    public static SimUKraftPacketHandler getNetwork() {
        return handler;
    }
}
