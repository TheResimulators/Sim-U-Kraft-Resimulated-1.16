package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileFarmer extends TileBaseMarker {
    private Seed seed = Seed.WHEAT;

    public TileFarmer() {
        super(ModTileEntities.FARMER.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) { // read
        if (compound.contains("seed")) {
            seed = Seed.getSeedById(compound.getInt("seed"));
        }
        super.load(state, compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if (seed != null) {
            compound.putInt("seed", seed.getId());
        }
        return super.save(compound);
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
        setChanged();
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        this.load(blockState, parentNBTTagCompound);
    }


    @Override
    public void fireSim() {
        setHired(false);
        setSimId(null);
    }
}