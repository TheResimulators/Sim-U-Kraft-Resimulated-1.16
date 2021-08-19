package com.resimulators.simukraft.common.enums;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public enum BuildingType {

    CUSTOM_DATA("custom Data", ModTileEntities.CUSTOM_DATA, Category.SPECIAL, 1),
    GLASS_FACTORY("glass factory", ModTileEntities.GLASS_FACTORY, Category.INDUSTRIAL, 2),
    COW_FARMER("cow farmer", ModTileEntities.COW_FARMER, Category.INDUSTRIAL, 3),
    SHEEP_FARMER("sheep farmer", ModTileEntities.SHEEP_FARMER, Category.INDUSTRIAL, 4),
    PIG_FARMER("pig farmer", ModTileEntities.PIG_FARMER, Category.INDUSTRIAL, 5),
    CHICKEN_FARMER("chicken farmer", ModTileEntities.CHICKEN_FARMER, Category.INDUSTRIAL, 6),
    BUTCHER("butchery", ModTileEntities.GLASS_FACTORY, Category.COMMERCIAL, 7),
    BAKER("bakery", ModTileEntities.BAKER, Category.COMMERCIAL, 8),
    GROCER("grocery", ModTileEntities.GLASS_FACTORY, Category.COMMERCIAL, 9),
    RESIDENTIAL("residential", ModTileEntities.RESIDENTIAL, Category.RESIDENTIAL, 10),
    SPECIAL("special", ModTileEntities.GLASS_FACTORY, Category.SPECIAL, 11),
    FISHERMAN("fisherman", ModTileEntities.FISHER_MAN, Category.INDUSTRIAL, 12);


    public String name;
    private RegistryObject<? extends TileEntityType<?>> type;
    public int id;
    public Category category;

    public static BuildingType getById(int id) {
        for (BuildingType type : BuildingType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public static BuildingType getByString(String string) {
        for (BuildingType type : BuildingType.values()) {
            if (type.name.equals(string)) {
                return type;
            }
        }
        return null;
    }

    public static int getMaxValueId() {
        return values().length - 1;

    }


    BuildingType(String name, RegistryObject<? extends TileEntityType<?>> type, Category category, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.category = category;

    }

    public RegistryObject<? extends TileEntityType<?>> getType() {
        return type;
    }

    public void setType(RegistryObject<? extends TileEntityType<?>> type) {
        this.type = type;
    }
}
