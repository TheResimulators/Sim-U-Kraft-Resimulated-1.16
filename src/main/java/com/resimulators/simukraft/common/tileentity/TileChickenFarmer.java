package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.common.enums.Animal;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;

public class TileChickenFarmer extends TileAnimalFarm {
    public TileChickenFarmer() {
        super(ModTileEntities.CHICKEN_FARMER.get(), Animal.CHICKEN, "Chicken Farm", Arrays.asList(Items.CHICKEN,Items.FEATHER));
    }
}
