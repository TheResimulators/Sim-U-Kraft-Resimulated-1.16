package com.resimulators.simukraft.common.enums;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;

public enum BuildingType {

    GLASS_FACTORY("glass factory", ModTileEntities.GLASS_FACTORY.get(),Category.INDUSTRIAL,1),
    COW_FARMER("cow farmer", ModTileEntities.COW_FARMER.get(),Category.INDUSTRIAL,2),
    SHEEP_FARMER("sheep farmer", ModTileEntities.SHEEP_FARMER.get(),Category.INDUSTRIAL,3),
    PIG_FARMER("pig farmer", ModTileEntities.PIG_FARMER.get(), Category.INDUSTRIAL, 4),
    CHICKEN_FARMER("chicken farmer", ModTileEntities.CHICKEN_FARMER.get(), Category.INDUSTRIAL, 5),
    BUTCHER("butchery", ModTileEntities.GLASS_FACTORY.get(), Category.COMMERCIAL,6),
    BAKER("bakery", ModTileEntities.GLASS_FACTORY.get(), Category.COMMERCIAL, 7),
    GROCER("grocery", ModTileEntities.GLASS_FACTORY.get(), Category.COMMERCIAL, 8),
    RESIDENTIAL("residential", ModTileEntities.GLASS_FACTORY.get(), Category.RESIDENTIAL,9),
    SPECIAL("special", ModTileEntities.GLASS_FACTORY.get(),Category.SPECIAL,10);




    public String name;
    public TileEntityType<?> type;
    public int id;
    public Category category;
    BuildingType(String name, TileEntityType<?> type, Category category,int id){
        this.name = name;
        this.type = type;
        this.id = id;
        this.category = category;

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
