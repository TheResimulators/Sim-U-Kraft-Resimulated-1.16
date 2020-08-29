package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class TileMiner extends TileBaseMarker {


    public TileMiner() {
        super(ModTileEntities.MINER.get());
    }



}