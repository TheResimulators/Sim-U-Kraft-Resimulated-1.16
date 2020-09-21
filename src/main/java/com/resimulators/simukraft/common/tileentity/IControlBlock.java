package com.resimulators.simukraft.common.tileentity;

import java.util.UUID;

public interface IControlBlock extends ITile {


    int getGui();

    @Override
    default void setHired(boolean hired) {

    }

    @Override
    default boolean getHired(){
        return false;
    }

    @Override
    default UUID getSimId(){
        return null;
    }

    @Override
    default void setSimId(UUID id){

    }
}
