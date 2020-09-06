package com.resimulators.simukraft.common.enums;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum Seed {

    WHEAT("wheat",0, Blocks.WHEAT,true,Type.CROP),
    POTATO("potato",1, Blocks.POTATOES, false,Type.CROP),
    CARROTS("carrot",2, Blocks.CARROTS, false, Type.CROP),
    PUMPKIN("pumpkin",3, Blocks.PUMPKIN,false,Type.OUTCROP),
    MELON("melon",4,Blocks.MELON,false,Type.OUTCROP),
    SUGAR_CANE("sugar cane",5,Blocks.SUGAR_CANE,false,Type.VERTICAL),
    CACTUS("cactus",6,Blocks.CACTUS,false,Type.VERTICAL);


    private final String name;
    private final int id;
    private final Block item;
    private final boolean enabled;
    private final Type type;
    Seed(String string, int id, Block item,boolean enabled,Type type){
        this.name = string;
        this.id = id;
        this.item = item;
        this.enabled = enabled;
        this.type = type;


    }
    private enum Type {
        CROP,
        OUTCROP,
        VERTICAL,


    }

    public static Seed getSeedById(int id){
        for (Seed seed: Seed.values()){
            if (seed.getId() == id){
                return seed;
            }
        }
        return null;
    }

    public static Seed getNextSeed(Seed seed){
        int id = seed.id;
        id = (id + 1) % Seed.values().length;
        return Seed.getSeedById(id);

    }

    public static Seed getNextEnabledSeed(Seed seed){
        seed = getNextSeed(seed);
        while (seed != null && !seed.enabled){
            if (seed.enabled){
                return seed;
            }else{
                seed = getNextSeed(seed);
            }

        }
        return seed;
    }
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Block getItem() {
        return item;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Type getType() {
        return type;
    }
}
