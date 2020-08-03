package com.resimulators.simukraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.UUID;

public class TileGlassFactory extends TileEntity implements ITile {
    public TileGlassFactory(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void setHired(boolean hired) {

    }

    @Override
    public boolean getHired() {
        return false;
    }

    @Override
    public UUID getSimId() {
        return null;
    }

    @Override
    public void setSimId(UUID id) {

    }
}
