package com.resimulators.simukraft.common.tileentity;

import java.util.UUID;

public interface ITile {

    boolean getHired();

    void setHired(boolean hired);

    UUID getSimId();

    void setSimId(UUID id);

    void fireSim();
}
