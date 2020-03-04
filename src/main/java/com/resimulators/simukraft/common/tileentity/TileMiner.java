package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileMiner extends TileEntity implements ITile {

    private boolean hired;
    private UUID simID;

    public TileMiner() {
        super(ModTileEntities.MINER);
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
    }

    @Override
    public boolean getHired() {
        return hired;
    }

    @Override
    public UUID getSimId() {
        return simID;
    }

    @Override
    public void setSimId(UUID id) {
        this.simID = id;
    }
}
