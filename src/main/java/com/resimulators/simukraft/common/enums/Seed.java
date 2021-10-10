package com.resimulators.simukraft.common.enums;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum Seed {

    WHEAT("wheat", 0, Blocks.WHEAT, true, Type.CROP),
    POTATO("potato", 1, Blocks.POTATOES, false, Type.CROP),
    CARROTS("carrot", 2, Blocks.CARROTS, false, Type.CROP),
    PUMPKIN("pumpkin", 3, Blocks.PUMPKIN, false, Type.OUTCROP),
    MELON("melon", 4, Blocks.MELON, false, Type.OUTCROP),
    SUGAR_CANE("sugar cane", 5, Blocks.SUGAR_CANE, false, Type.VERTICAL),
    CACTUS("cactus", 6, Blocks.CACTUS, false, Type.VERTICAL);


    private final String name;
    private final int id;
    private final Block item;
    private final boolean enabled;
    private final Type type;

    /** gets the next seed that is enabled, could become configurable */
    public static Seed getNextEnabledSeed(Seed seed) {
        seed = getNextSeed(seed);
        while (seed != null && !seed.enabled) {
            seed = getNextSeed(seed);

        }
        return seed;
    }

    /** gets the next id in the list, used in Gui to cycle through them */
    public static Seed getNextSeed(Seed seed) {
        int id = seed.id;
        id = (id + 1) % Seed.values().length;
        return Seed.getSeedById(id);

    }

    /** gets the seed by id, checks each one for the correct one with given id */
    public static Seed getSeedById(int id) {
        for (Seed seed : Seed.values()) {
            if (seed.getId() == id) {
                return seed;
            }
        }
        return null;
    }

    /** gets id of Seed instance */
    public int getId() {
        return id;
    }

    Seed(String string, int id, Block item, boolean enabled, Type type) {
        this.name = string;
        this.id = id;
        this.item = item;
        this.enabled = enabled;
        this.type = type;


    }

    /** gets name of Seed instance */
    public String getName() {
        return name;
    }

    /** gets block associated with Seed */
    public Block getBlock() {
        return item;
    }

    /** returns if Seed instance is enabled */
    public boolean isEnabled() {
        return enabled;
    }

    /** gets the type of planting Seed instance is */
    public Type getType() {
        return type;
    }

    private enum Type {
        CROP,
        OUTCROP,
        VERTICAL,


    }
}
