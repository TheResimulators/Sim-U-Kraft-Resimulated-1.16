package com.Resimulators.simukraft.common.tileentity;

import com.Resimulators.simukraft.init.ModTileEntities;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    public TileConstructor() {
        super(ModTileEntities.CONSTRUCTOR);
    }

    @Override
    public CompoundNBT getUpdateTag(){
        return write(new CompoundNBT());
    }
    @Override
    public void handleUpdateTag(CompoundNBT nbt){
        read(nbt);
    }


    @Override
    public CompoundNBT write(CompoundNBT nbt){
        nbt.putBoolean("hired",hired);
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt){

    }
    @Override
    public void setHired(boolean hired){
        this.hired = hired;
    }
    @Override
    public boolean getHired(){
        return hired;
    }
}
