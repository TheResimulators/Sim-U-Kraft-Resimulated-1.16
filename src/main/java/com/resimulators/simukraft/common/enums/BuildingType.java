package com.resimulators.simukraft.common.enums;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;

public enum BuildingType {

    GLASS_FACTORY("glass factory", ModTileEntities.GLASS_FACTORY.get(),1),
    COW_FARMER("cow farmer", ModTileEntities.GLASS_FACTORY.get(),2),
    SHEEP_FARMER("sheep farmer", ModTileEntities.GLASS_FACTORY.get(),3),
    PIG_FARMER("pig farmer", ModTileEntities.GLASS_FACTORY.get(),4),
    CHICKEN_FARMER("chicken farmer", ModTileEntities.GLASS_FACTORY.get(),5),
    BUTCHER("butchery", ModTileEntities.GLASS_FACTORY.get(),6),
    BAKER("bakery", ModTileEntities.GLASS_FACTORY.get(),7),
    GROCER("grocery", ModTileEntities.GLASS_FACTORY.get(),8);




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
    public static BuildingType getByString(String string){
        for (BuildingType type: BuildingType.values()){
            if (type.name.equals(string)){
                return type;
            }
        }
        return null;
    }

}
