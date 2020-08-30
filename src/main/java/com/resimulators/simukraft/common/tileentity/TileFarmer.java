package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import java.util.UUID;

public class TileFarmer extends TileBaseMarker {
    private Seed seed = Seed.WHEAT;
    public TileFarmer() {
        super(ModTileEntities.FARMER.get());
    }


    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (seed != null){
            compound.putInt("seed",seed.getId());
        }
        return super.write(compound);
    }


    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound) { // read
        if (compound.contains("seed")){
            seed = Seed.getSeedById(compound.getInt("seed"));
        }
        super.func_230337_a_(state, compound);
    }


    public void setSeed(Seed seed) {
        this.seed = seed;
        markDirty();
    }

    public Seed getSeed(){
        return seed;
    }
}