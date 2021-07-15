package com.resimulators.simukraft.utils;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

public class ColorHelper {
    public static int convertDyeToTF(int i) {
        return Colors.getFromDyeColor(i).getTfColor();
    }

    public static int convertTFToDye(int i) {
        return Colors.getFromTFColor(i).getDyeColor();
    }

    public static Color getColorFromDye(int i) {
        return Colors.getFromDyeColor(i).getColor();
    }

    public static Color getColorFromTF(int i) {
        return Colors.getFromTFColor(i).getColor();
    }

    private enum Colors {
        WHITE(0, 15, Color.WHITE), //White
        ORANGE(1, 6, new Color(255, 170, 0)), //Gold
        MAGENTA(2, 13, new Color(255, 85, 255)), //Light Purple
        LIGHT_BLUE(3, 11, new Color(85, 255, 255)), //Aqua
        YELLOW(4, 14, new Color(255, 255, 85)), //Yellow
        LIME(5, 10, new Color(85, 255, 85)), //Green
        PINK(6, 12, new Color(255, 85, 85)), //Red
        GRAY(7, 8, new Color(85, 85, 85)), //Dark Gray
        LIGHT_GRAY(8, 7, new Color(170, 170, 170)), //Gray
        CYAN(9, 3, new Color(0, 170, 170)), //Dark Aqua
        PURPLE(10, 5, new Color(170, 0, 170)), //Dark Purple
        BLUE(11, 1, new Color(0, 0, 170)), //Dark Blue
        BROWN(12, 6, new Color(255, 170, 10)), //Gold
        GREEN(13, 2, new Color(0, 170, 0)), //Dark Green
        RED(14, 4, new Color(170, 0, 0)), //Dark Red
        BLACK(15, 0, Color.BLACK); //Black

        private static final Colors[] DYECOLORS = Arrays.stream(values()).sorted(Comparator.comparingInt(Colors::getDyeColor)).toArray(Colors[]::new);
        private static final Colors[] TFCOLORS = Arrays.stream(values()).sorted(Comparator.comparingInt(Colors::getTfColor)).toArray(Colors[]::new);

        int dyeColor;
        int tfColor;
        Color color;

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

        Colors(int dyeColor, int tfColor, Color color) {
            this.dyeColor = dyeColor;
            this.tfColor = tfColor;
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public int getDyeColor() {
            return dyeColor;
        }

        public int getTfColor() {
            return tfColor;
        }
    }
}
