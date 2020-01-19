package com.Resimulators.simukraft.utils;

import java.util.Arrays;
import java.util.Comparator;

public class ColorHelper {
    public static int convertDyeToTF(int i) {
        return Colors.getFromDyeColor(i).getTfColor();
    }

    public static int convertTFToDye(int i) {
        return Colors.getFromTFColor(i).getDyeColor();
    }

    private enum Colors {
        WHITE(0, 15), //White
        ORANGE(1, 6), //Gold
        MAGENTA(2, 13), //Light Purple
        LIGHT_BLUE(3, 11), //Aqua
        YELLOW(4, 14), //Yellow
        LIME(5, 10), //Green
        PINK(6, 12), //Red
        GRAY(7, 8), //Dark Gray
        LIGHT_GRAY(8, 7), //Gray
        CYAN(9, 3), //Dark Aqua
        PURPLE(10, 5), //Dark Purple
        BLUE(11, 1), //Dark Blue
        BROWN(12, 6), //Gold
        GREEN(13, 2), //Dark Green
        RED(14, 4), //Dark Red
        BLACK(15, 0); //Black

        private static final Colors[] DYECOLORS = Arrays.stream(values()).sorted(Comparator.comparingInt(Colors::getDyeColor)).toArray(Colors[]::new);
        private static final Colors[] TFCOLORS = Arrays.stream(values()).sorted(Comparator.comparingInt(Colors::getTfColor)).toArray(Colors[]::new);

        int dyeColor;
        int tfColor;
        Colors(int dyeColor, int tfColor) {
            this.dyeColor = dyeColor;
            this.tfColor = tfColor;
        }

        public int getDyeColor() {
            return dyeColor;
        }

        public int getTfColor() {
            return tfColor;
        }

        public static Colors getFromDyeColor(int dyeColor) {
            if (dyeColor < 0 || dyeColor >= DYECOLORS.length) {
                dyeColor = 0;
            }
            return DYECOLORS[dyeColor];
        }

        public static Colors getFromTFColor(int tfColor) {
            if (tfColor < 0 || tfColor >= TFCOLORS.length) {
                tfColor = 0;
            }
            return TFCOLORS[tfColor];
        }
    }
}
