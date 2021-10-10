package com.resimulators.simukraft.common.entity.sim;

import com.mojang.datafixers.util.Pair;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.init.ModContainers;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
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
    private final SimEntity sim;
    private final PlayerEntity player;
    public String job = Profession.UNEMPLOYED.getName();
    SimInventory inventory;

    public SimContainer(int windowID, boolean localWorld, SimEntity sim, PlayerInventory playerInventory) {
        super(ModContainers.SIM_CONTAINER.get(), windowID);
        this.isLocalWorld = localWorld;
        inventory = sim.getInventory();
        this.sim = sim;
        this.player = playerInventory.player;

        for (int i = 0; i < 4; i++) {
            final EquipmentSlotType equipmentSlotType = VALID_EQUIPMENT_SLOTS[i];
            this.addSlot(new Slot(inventory, 30 - i, 8, 8 + i * 18) {
                public boolean mayPlace(ItemStack stack) {
                    return stack.canEquip(equipmentSlotType, sim);
                }

                public int getMaxStackSize() {
                    return 1;
                }

                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(SimContainer.blocks, SimContainer.ARMOR_SLOT_TEXTURES[equipmentSlotType.getIndex()]);
                }

                public boolean mayPickup(PlayerEntity player) {
                    ItemStack itemStack = this.getItem();
                    return (itemStack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && super.mayPickup(player);
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

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 200));
        }

        this.addSlot(new Slot(inventory, 31, 77, 62) {
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(SimContainer.blocks, SimContainer.shield);
            }
        });

        addDataSlots(new IIntArray() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0:
                        return getSim().getSpecial() ? 1 : 0;
                    case 1:
                        return getSim().getFemale() ? 1 : 0;
                    case 2:
                        return getSim().getVariation();
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        getSim().setSpecial(value == 1);
                        break;
                    case 1:
                        getSim().setFemale(value == 1);
                        break;
                    case 2:
                        getSim().setVariation(value);
                        break;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        addDataSlot(new IntReferenceHolder() {
            @Override
            public int get() {
                if (sim.getJob() != null) {
                    return sim.getProfession();
                } else {
                    return Profession.UNEMPLOYED.getId();
                }
            }

            @Override
            public void set(int id) {
                job = Profession.getNameFromID(id);
            }
        });
    }

    public SimEntity getSim() {
        return sim;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(itemstack);
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index < 5) {
                if (!this.moveItemStackTo(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR && !this.slots.get(equipmentslottype.getIndex()).hasItem()) {
                int i = Utils.getReversedInt(4, equipmentslottype.getIndex());
                if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.slots.get(67).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 67, 68, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 67) {
                if (!this.moveItemStackTo(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 31) {
                if (!this.moveItemStackTo(itemstack1, 31, 67, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 67) {
                if (!this.moveItemStackTo(itemstack1, 0, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 31, 67, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
            if (index == 0) {
                playerIn.drop(itemstack2, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }
}
