package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Collections;

public class TilePigFarmer extends TileAnimalFarm {
    public TilePigFarmer() {
        super(ModTileEntities.PIG_FARMER.get(), Animal.PIG, "Pig Farm", Collections.singletonList(Items.PORKCHOP));
    }
}
