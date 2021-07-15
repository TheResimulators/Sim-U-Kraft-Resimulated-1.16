package com.resimulators.simukraft.common.item.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public interface IStructureStorage {
    /** gets structure from item stack if it exists */
    default String getStructure(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() ? stack.getTag().getString("structure") : "";
    }

    /** sets structure on item stack */
    @Nonnull
    default ItemStack setStructure(ItemStack stack, String name) {
        if (!stack.isEmpty()) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }
            stack.getTag().putString("structure", name);
        }
        return stack;
    }
}
