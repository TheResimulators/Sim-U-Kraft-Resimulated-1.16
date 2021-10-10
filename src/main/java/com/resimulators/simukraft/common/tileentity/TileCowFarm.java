package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;

public class TileCowFarm extends TileAnimalFarm {

    public TileCowFarm() {
        super(ModTileEntities.COW_FARMER.get(), Animal.COW, "Cow Farm");
    }
}
