package com.resimulators.simukraft.common.tileentity;

import java.util.UUID;

public interface IControlBlock extends ITile {


    int getGui();

    @Override
    boolean getHired();

    @Override
    void setHired(boolean hired);

    @Override
    UUID getSimId();


    @Override
    void setSimId(UUID id);

    String getName();
}
