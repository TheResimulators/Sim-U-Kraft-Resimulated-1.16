package com.Resimulators.simukraft.common.tileentity;

import java.util.UUID;

public interface ITile {

    void setHired(boolean hired);

    boolean getHired();

    UUID getSimId();

    void setSimId(UUID id);
}
