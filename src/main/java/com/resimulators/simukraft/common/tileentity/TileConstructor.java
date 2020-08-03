package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    private UUID simID;

    public TileConstructor() {
        super(ModTileEntities.CONSTRUCTOR.get());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putBoolean("hired", hired);
        if (simID != null) {
            nbt.putUniqueId("simid", simID);
        }
        return nbt;
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT nbt) {
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simID = nbt.getUniqueId("simid");
        }
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        markDirty();
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
        markDirty();
    }

}
