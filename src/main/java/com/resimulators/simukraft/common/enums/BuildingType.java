package com.resimulators.simukraft.common.enums;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;

public enum BuildingType {

    GLASS_FACTORY("glass factory", ModTileEntities.GLASS_FACTORY.get(),1);





    public String name;
    public TileEntityType<?> type;
    public int id;
    BuildingType(String name, TileEntityType<?> type,int id){
        this.name = name;
        this.type = type;
        this.id = id;

    }



    public static BuildingType getById(int id){
        for (BuildingType type: BuildingType.values()){
            if (type.id == id){
                return type;
            }
        }
        return null;
    }
}
