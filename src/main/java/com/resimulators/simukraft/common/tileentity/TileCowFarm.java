package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.item.Items;

import java.util.Arrays;

public class TileCowFarm extends TileAnimalFarm {

    public TileCowFarm() {
        super(ModTileEntities.COW_FARMER.get(), Animal.COW, "Cow Farm", Arrays.asList(Items.LEATHER,Items.BEEF));
    }
}
