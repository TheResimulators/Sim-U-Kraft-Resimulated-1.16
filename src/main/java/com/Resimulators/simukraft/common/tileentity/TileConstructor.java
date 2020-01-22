package com.Resimulators.simukraft.common.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileConstructor extends TileEntity {

    private boolean hired;
    public TileConstructor(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
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

}
