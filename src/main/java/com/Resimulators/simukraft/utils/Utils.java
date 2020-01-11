package com.Resimulators.simukraft.utils;

import java.util.Random;

public class Utils {
    private static Random rand = new Random();

    public static boolean randomizeBoolean() {
        int dice = rand.nextInt(2);
        return dice == 1;
    }

    public static boolean randomizeBooleanWithChance(int i) {
        int dice = rand.nextInt(i);
        return dice == 0;
    }
}
