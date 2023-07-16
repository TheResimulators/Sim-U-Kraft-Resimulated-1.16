package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Collections;

public class TileSheepFarm extends TileAnimalFarm {
    public TileSheepFarm() {
        super(ModTileEntities.SHEEP_FARMER.get(), Animal.SHEEP, "Sheep Farm", Collections.singletonList(Items.MUTTON));
    }
}
