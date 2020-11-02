package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;

public class TileChickenFarmer extends TileAnimalFarm{
    public TileChickenFarmer() {
        super(ModTileEntities.CHICKEN_FARMER.get(), Animal.CHICKEN, "Chicken Farm");
    }
}
