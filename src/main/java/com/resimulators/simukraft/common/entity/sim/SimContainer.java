package com.resimulators.simukraft.common.entity.sim;

import com.resimulators.simukraft.init.OHRegistry;
import com.resimulators.simukraft.utils.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SimContainer extends Container {
    public static final ResourceLocation blocks = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation helmet = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation chestplate = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation leggings = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation boots = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation shield = new ResourceLocation("item/empty_armor_slot_shield");
    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{boots, leggings, chestplate, helmet};
    private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
    public final boolean isLocalWorld;
    private final EntitySim sim;
    private final PlayerEntity player;

    public SimContainer(int windowID, boolean localWorld, EntitySim sim, PlayerInventory playerInventory) {
        super(OHRegistry.simContainer, windowID);
        this.isLocalWorld = localWorld;
        SimInventory inventory = sim.getInventory();
        this.sim = sim;
        this.player = playerInventory.player;

        for (int i = 0; i < 4; i++) {
            final EquipmentSlotType equipmentSlotType = VALID_EQUIPMENT_SLOTS[i];
            this.addSlot(new Slot(inventory, 30 - i, 8, 8 + i * 18) {
                public int getSlotStackLimit() {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack) {
                    return stack.canEquip(equipmentSlotType, sim);
                }

                public boolean canTakeStack(PlayerEntity player) {
                    ItemStack itemStack = this.getStack();
                    return (itemStack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && super.canTakeStack(player);
                }

                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
                    return Pair.of(SimContainer.blocks, SimContainer.ARMOR_SLOT_TEXTURES[equipmentSlotType.getIndex()]);
                }
            });
        }

        for (int l = 0; l < 3; l++) {
            for (int li = 0; li < 9; li++) {
                this.addSlot(new Slot(inventory, li + l * 9, 8 + li * 18, 84 + l * 18));
            }
        }

        for (int l = 0; l < 3; l++) {
            for (int li = 0; li < 9; li++) {
                this.addSlot(new Slot(playerInventory, li + (l + 1) * 9, 8 + li * 18, 142 + l * 18));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 200));
        }

        this.addSlot(new Slot(inventory, 31, 77, 62) {
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
                return Pair.of(SimContainer.blocks, SimContainer.shield);
            }
        });
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
            if (index == 0) {
                if (!this.mergeItemStack(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index < 5) {
                if (!this.mergeItemStack(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(equipmentslottype.getIndex()).getHasStack()) {
                int i = Utils.getReversedInt(4, equipmentslottype.getIndex());
                if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(67).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 67, 68, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 67) {
                if (!this.mergeItemStack(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 31) {
                if (!this.mergeItemStack(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 67) {
                if (!this.mergeItemStack(itemstack1, 0, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 31, 67, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
            if (index == 0) {
                playerIn.dropItem(itemstack2, false);
            }
        }

        return itemstack;
    }

    public EntitySim getSim() {
        return sim;
    }
}
