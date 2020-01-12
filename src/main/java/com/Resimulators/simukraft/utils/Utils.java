package com.Resimulators.simukraft.utils;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
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

    public static boolean canInsertStack(IItemHandler handler, @Nonnull ItemStack stack) {
        final ItemStack toInsert = ItemHandlerHelper.insertItemStacked(handler, stack, true);
        return toInsert.getCount() < stack.getCount();
    }

    public static void setEntityItemStack(ItemEntity entity, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            entity.remove();
        } else {
            entity.setItem(stack);
        }
    }

    public static float round(float d, int decimalPlace) {
        try {
            BigDecimal bd = new BigDecimal(Float.toString(d));
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
            return bd.floatValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
