package com.Resimulators.simukraft.common.entity.sim;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class SimInventory implements IInventory, INamedContainerProvider {
    private String inventoryTitle;
    private final int slotsCount;
    private final NonNullList<ItemStack> inventoryContents;
    /** Listeners notified when any item in this inventory is changed. */
    private List<IInventoryChangedListener> changeListeners;
    private boolean hasCustomName;

    public SimInventory(String title, boolean customName, int slotCount) {
        this.inventoryTitle = title;
        this.hasCustomName = customName;
        this.slotsCount = slotCount;
        this.inventoryContents = NonNullList.<ItemStack>withSize(slotCount, ItemStack.EMPTY);
    }

    @OnlyIn(Dist.CLIENT)
    public SimInventory(ITextComponent title, int slotCount) {
        this(title.getUnformattedComponentText(), true, slotCount);
    }

    public void addInventoryChangeListener(IInventoryChangedListener listener) {
        if (this.changeListeners == null) {
            this.changeListeners = Lists.<IInventoryChangedListener>newArrayList();
        }
        this.changeListeners.add(listener);
    }

    public void removeInventoryChangeListener(IInventoryChangedListener listener) {
        this.changeListeners.remove(listener);
    }

    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.inventoryContents.size() ? (ItemStack)this.inventoryContents.get(index) : ItemStack.EMPTY;
    }

    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }

    public ItemStack addItem(ItemStack stack) {
        ItemStack itemstack = stack.copy();
        for (int i = 0; i < this.slotsCount; ++i) {
            ItemStack itemstack1 = this.getStackInSlot(i);
            if (itemstack1.isEmpty()) {
                this.setInventorySlotContents(i, itemstack);
                this.markDirty();
                return ItemStack.EMPTY;
            }
            if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
                int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());
                if (k > 0) {
                    itemstack1.grow(k);
                    itemstack.shrink(k);
                    if (itemstack.isEmpty()) {
                        this.markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        if (itemstack.getCount() != stack.getCount()) {
            this.markDirty();
        }
        return itemstack;
    }

    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = this.inventoryContents.get(index);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.inventoryContents.set(index, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventoryContents.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    public int getSizeInventory() {
        return this.slotsCount;
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventoryContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return this.inventoryTitle;
    }

    public boolean hasCustomName() {
        return this.hasCustomName;
    }

    public void setCustomName(String inventoryTitleIn) {
        this.hasCustomName = true;
        this.inventoryTitle = inventoryTitleIn;
    }

    public ITextComponent getDisplayName() {
        return (ITextComponent)(this.hasCustomName() ? new StringTextComponent(this.getName()) : new TranslationTextComponent(this.getName(), new Object[0]));
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public void markDirty() {
        if (this.changeListeners != null) {
            for (int i = 0; i < this.changeListeners.size(); ++i) {
                ((IInventoryChangedListener) this.changeListeners.get(i)).onInventoryChanged(this);
            }
        }
    }

    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    public void openInventory(PlayerEntity player) {

    }

    public void closeInventory(PlayerEntity player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {
    }

    public int getFieldCount() {
        return 0;
    }

    public void clear() {
        this.inventoryContents.clear();
    }

    public ListNBT write(ListNBT nbt) {
        for (int i = 0; i < this.inventoryContents.size(); i++) {
            if (!this.inventoryContents.get(i).isEmpty()) {
                CompoundNBT compoundNBT = new CompoundNBT();
                compoundNBT.putByte("Slot", (byte)i);
                this.inventoryContents.get(i).write(compoundNBT);
                nbt.add(compoundNBT);
            }
        }
        return nbt;
    }

    public void read(ListNBT nbt) {
        for(int i = 0; i < nbt.size(); i++) {
            CompoundNBT compoundNBT = nbt.getCompound(i);
            int j = compoundNBT.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.read(compoundNBT);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.inventoryContents.size()) {
                    this.inventoryContents.set(j, itemStack);
                }
            }
        }
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return ChestContainer.createGeneric9X3(i, playerInventory, this);
    }
}
