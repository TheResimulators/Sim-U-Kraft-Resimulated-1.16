package com.resimulators.simukraft.common.entity.sim;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;

public class SimArmorInvWrapper extends RangedWrapper {
    private final SimInventory inventory;

    public SimArmorInvWrapper(SimInventory inventory) {
        super(new InvWrapper(inventory), inventory.mainInventory.size(), inventory.mainInventory.size() + inventory.armorInventory.size());
        this.inventory = inventory;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        EquipmentSlotType equipmentSlotType = null;
        for (EquipmentSlotType s : EquipmentSlotType.values()) {
            if (s.getType() == EquipmentSlotType.Group.ARMOR && s.getIndex() == slot) {
                equipmentSlotType = s;
                break;
            }
        }

        if (equipmentSlotType != null && slot < 4 && !stack.isEmpty() && stack.canEquip(equipmentSlotType, inventory.getSim())) {
            return super.insertItem(slot, stack, simulate);
        }

        return stack;
    }
}
