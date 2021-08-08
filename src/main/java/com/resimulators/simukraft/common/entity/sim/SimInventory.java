package com.resimulators.simukraft.common.entity.sim;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class SimInventory implements IInventory, INamedContainerProvider {
    public final NonNullList<ItemStack> mainInventory;
    public final NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> handInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private final int slotsCount;
    private final List<NonNullList<ItemStack>> allInventories;
    private final SimEntity sim;
    public int currentItem;
    private String inventoryTitle;
    private ItemStack itemStack = ItemStack.EMPTY;
    private int timesChanged;

    /** Listeners notified when any item in this inventory is changed. */
    private List<IInventoryChangedListener> changeListeners;
    private boolean hasCustomName;
    private IItemHandlerModifiable handler;

    public static int getHotbarSize() {
        return 27;
    }

    public SimInventory(SimEntity sim, String title, boolean customName, int slotCount) {
        this.sim = sim;
        this.inventoryTitle = title;
        this.hasCustomName = customName;
        this.slotsCount = slotCount + 5;
        this.mainInventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.allInventories = ImmutableList.of(this.mainInventory, this.armorInventory, this.handInventory);
    }

    public ItemStack getCurrentItem() {
        return isHotbar(this.currentItem) ? this.mainInventory.get(this.currentItem) : ItemStack.EMPTY;
    }

    public static boolean isHotbar(int index) {
        return index >= 0 && index < 27;
    }

    private boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
        return !stack1.isEmpty() && this.stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < this.getMaxStackSize();
    }

    @OnlyIn(Dist.CLIENT)
    public void setPickedItemStack(ItemStack stack) {
        int i = this.getSlotFor(stack);
        if (isHotbar(i)) {
            this.currentItem = i;
        } else {
            if (i == -1) {
                this.currentItem = this.getBestHotbarSlot();
                if (!this.mainInventory.get(this.currentItem).isEmpty()) {
                    int j = this.getFirstEmptyStack();
                    if (j != -1) {
                        this.mainInventory.set(j, this.mainInventory.get(this.currentItem));
                    }
                }

                this.mainInventory.set(this.currentItem, stack);
            } else {
                this.pickItem(i);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getSlotFor(ItemStack stack) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).isEmpty() && this.stackEqualExact(stack, this.mainInventory.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public int getBestHotbarSlot() {
        for (int i = 0; i < 9; ++i) {
            int j = (this.currentItem + i) % 9;
            if (this.mainInventory.get(j).isEmpty()) {
                return j;
            }
        }

        for (int k = 0; k < 9; ++k) {
            int l = (this.currentItem + k) % 9;
            if (!this.mainInventory.get(l).isEnchanted()) {
                return l;
            }
        }

        return this.currentItem;
    }

    public int getFirstEmptyStack() {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (this.mainInventory.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public void pickItem(int index) {
        this.currentItem = this.getBestHotbarSlot();
        ItemStack itemstack = this.mainInventory.get(this.currentItem);
        this.mainInventory.set(this.currentItem, this.mainInventory.get(index));
        this.mainInventory.set(index, itemstack);
    }

    private boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && ItemStack.tagMatches(stack1, stack2);
    }

    public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            ItemStack itemstack = this.mainInventory.get(i);
            if (!this.mainInventory.get(i).isEmpty() && this.stackEqualExact(p_194014_1_, this.mainInventory.get(i)) && !this.mainInventory.get(i).isDamaged() && !itemstack.isEnchanted() && !itemstack.hasCustomHoverName()) {
                return i;
            }
        }

        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    public void changeCurrentItem(double direction) {
        if (direction > 0.0D) {
            direction = 1.0D;
        }

        if (direction < 0.0D) {
            direction = -1.0D;
        }

        for (this.currentItem = (int) ((double) this.currentItem - direction); this.currentItem < 0; this.currentItem += 9) {
        }

        while (this.currentItem >= 9) {
            this.currentItem -= 9;
        }

    }

    public int clearMatchingItems(Predicate<ItemStack> p_195408_1_, int count) {
        int i = 0;

        for (int j = 0; j < this.getContainerSize(); ++j) {
            ItemStack itemstack = this.getItem(j);
            if (!itemstack.isEmpty() && p_195408_1_.test(itemstack)) {
                int k = count <= 0 ? itemstack.getCount() : Math.min(count - i, itemstack.getCount());
                i += k;
                if (count != 0) {
                    itemstack.shrink(k);
                    if (itemstack.isEmpty()) {
                        this.setItem(j, ItemStack.EMPTY);
                    }

                    if (count > 0 && i >= count) {
                        return i;
                    }
                }
            }
        }

        if (!this.itemStack.isEmpty() && p_195408_1_.test(this.itemStack)) {
            int l = count <= 0 ? this.itemStack.getCount() : Math.min(count - i, this.itemStack.getCount());
            i += l;
            if (count != 0) {
                this.itemStack.shrink(l);
                if (this.itemStack.isEmpty()) {
                    this.itemStack = ItemStack.EMPTY;
                }

                if (count > 0 && i >= count) {
                    return i;
                }
            }
        }

        return i;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getContainerSize() {
        return this.mainInventory.size() + this.armorInventory.size() + this.handInventory.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.mainInventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.armorInventory) {
            if (!itemstack1.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.handInventory) {
            if (!itemstack2.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getItem(int index) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list == null ? ItemStack.EMPTY : list.get(index);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack removeItem(int index, int count) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list != null && !list.get(index).isEmpty() ? ItemStackHelper.removeItem(list, index, count) : ItemStack.EMPTY;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeItemNoUpdate(int index) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null && !nonnulllist.get(index).isEmpty()) {
            ItemStack itemstack = nonnulllist.get(index);
            nonnulllist.set(index, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setItem(int index, ItemStack stack) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null) {
            nonnulllist.set(index, stack);
        }

    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void setChanged() {
        ++this.timesChanged;
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    public boolean stillValid(PlayerEntity player) {
        if (this.sim.removed) {
            return false;
        } else {
            return !(player.distanceToSqr(this.sim) > 64.0D);
        }
    }

    private int storePartialItemStack(ItemStack itemStackIn) {
        int i = this.storeItemStack(itemStackIn);
        if (i == -1) {
            i = this.getFirstEmptyStack();
        }

        return i == -1 ? itemStackIn.getCount() : this.addResource(i, itemStackIn);
    }

    private int addResource(int index, ItemStack itemStack) {
        Item item = itemStack.getItem();
        int i = itemStack.getCount();
        ItemStack itemstack = this.getItem(index);
        if (itemstack.isEmpty()) {
            itemstack = itemStack.copy();
            itemstack.setCount(0);
            if (itemStack.hasTag()) {
                itemstack.setTag(itemStack.getTag().copy());
            }

            this.setItem(index, itemstack);
        }

        int j = i;
        if (i > itemstack.getMaxStackSize() - itemstack.getCount()) {
            j = itemstack.getMaxStackSize() - itemstack.getCount();
        }

        if (j > this.getMaxStackSize() - itemstack.getCount()) {
            j = this.getMaxStackSize() - itemstack.getCount();
        }

        if (j == 0) {
            return i;
        } else {
            i = i - j;
            itemstack.grow(j);
            itemstack.setPopTime(5);
            return i;
        }
    }

    public int storeItemStack(ItemStack itemStackIn) {
        if (this.canMergeStacks(this.getItem(this.currentItem), itemStackIn)) {
            return this.currentItem;
        } else if (this.canMergeStacks(this.getItem(40), itemStackIn)) {
            return 40;
        } else {
            for (int i = 0; i < this.mainInventory.size(); ++i) {
                if (this.canMergeStacks(this.mainInventory.get(i), itemStackIn)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void tick() {
        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (!nonnulllist.get(i).isEmpty()) {
                    nonnulllist.get(i).inventoryTick(this.sim.level, this.sim, i, this.currentItem == i);
                }
            }
        }
        //armorInventory.forEach(e -> e.onArmorTick(sim.world, sim));
    }

    public boolean addItemStackToInventory(ItemStack itemStackIn) {
        return this.add(-1, itemStackIn);
    }

    public boolean add(int slotIn, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            try {
                if (stack.isDamaged()) {
                    if (slotIn == -1) {
                        slotIn = this.getFirstEmptyStack();
                    }

                    if (slotIn >= 0) {
                        this.mainInventory.set(slotIn, stack.copy());
                        this.mainInventory.get(slotIn).setPopTime(5);
                        stack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = stack.getCount();
                        if (slotIn == -1) {
                            stack.setCount(this.storePartialItemStack(stack));
                        } else {
                            stack.setCount(this.addResource(slotIn, stack));
                        }

                    } while (!stack.isEmpty() && stack.getCount() < i);

                    return stack.getCount() < i;
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Class", () -> stack.getItem().getClass().getName());
                crashreportcategory.setDetail("Item ID", Item.getId(stack.getItem()));
                crashreportcategory.setDetail("Item data", stack.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> stack.getHoverName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    public void placeItemBackInInventory(World worldIn, ItemStack stack) {
        if (!worldIn.isClientSide) {
            while (!stack.isEmpty()) {
                int i = this.storeItemStack(stack);
                if (i == -1) {
                    i = this.getFirstEmptyStack();
                }

                if (i == -1) {
                    this.sim.dropItem(stack, false, false);
                    break;
                }

                int j = stack.getMaxStackSize() - this.getItem(i).getCount();
                if (this.add(i, stack.split(j))) {
                    //((EntitySim)this.sim).connection.sendPacket(new SSetSlotPacket(-2, i, this.getStackInSlot(i)));
                }
            }

        }
    }

    public void deleteStack(ItemStack stack) {
        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (nonnulllist.get(i) == stack) {
                    nonnulllist.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }

    }

    public float getDestroySpeed(BlockState state) {
        return this.mainInventory.get(this.currentItem).getDestroySpeed(state);
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     */
    public ListNBT write(ListNBT nbtTagListIn) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).isEmpty()) {
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putByte("Slot", (byte) i);
                this.mainInventory.get(i).save(compoundnbt);
                nbtTagListIn.add(compoundnbt);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j) {
            if (!this.armorInventory.get(j).isEmpty()) {
                CompoundNBT compoundnbt1 = new CompoundNBT();
                compoundnbt1.putByte("Slot", (byte) (j + 100));
                this.armorInventory.get(j).save(compoundnbt1);
                nbtTagListIn.add(compoundnbt1);
            }
        }

        for (int k = 0; k < this.handInventory.size(); ++k) {
            if (!this.handInventory.get(k).isEmpty()) {
                CompoundNBT compoundnbt2 = new CompoundNBT();
                compoundnbt2.putByte("Slot", (byte) (k + 150));
                this.handInventory.get(k).save(compoundnbt2);
                nbtTagListIn.add(compoundnbt2);
            }
        }

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     */
    public void read(ListNBT nbtTagListIn) {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.handInventory.clear();

        for (int i = 0; i < nbtTagListIn.size(); ++i) {
            CompoundNBT compoundnbt = nbtTagListIn.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.of(compoundnbt);
            if (!itemstack.isEmpty()) {
                if (j >= 0 && j < this.mainInventory.size()) {
                    this.mainInventory.set(j, itemstack);
                } else if (j >= 100 && j < this.armorInventory.size() + 100) {
                    this.armorInventory.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.handInventory.size() + 150) {
                    this.handInventory.set(j - 150, itemstack);
                }
            }
        }

    }

    public ITextComponent getName() {
        return new TranslationTextComponent("container.inventory");
    }

    public boolean canHarvestBlock(BlockState state) {
        return this.getItem(this.currentItem).isCorrectToolForDrops(state);
    }

    /**
     * returns a sim armor item (as itemstack) contained in specified armor slot.
     */
    @OnlyIn(Dist.CLIENT)
    public ItemStack armorItemInSlot(int slotIn) {
        return this.armorInventory.get(slotIn);
    }

    /**
     * Damages armor in each slot by the specified amount.
     */
    public void damageArmor(float damage) {
        if (!(damage <= 0.0F)) {
            damage = damage / 4.0F;
            if (damage < 1.0F) {
                damage = 1.0F;
            }

            for (int i = 0; i < this.armorInventory.size(); ++i) {
                ItemStack itemstack = this.armorInventory.get(i);
                if (itemstack.getItem() instanceof ArmorItem) {
                    int j = i;
                    itemstack.hurtAndBreak((int) damage, this.sim, (p_214023_1_) -> {
                        p_214023_1_.broadcastBreakEvent(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, j));
                    });
                }
            }

        }
    }

    /**
     * Drop all armor and main inventory items.
     */
    public void dropAllItems() {
        for (List<ItemStack> list : this.allInventories) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemstack = list.get(i);
                if (!itemstack.isEmpty()) {
                    this.sim.dropItem(itemstack, true, false);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public int getTimesChanged() {
        return this.timesChanged;
    }

    /**
     * Stack helds by mouse, used in GUI and Containers
     */
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Set the stack helds by mouse, used in GUI/Container
     */
    public void setItemStack(ItemStack itemStackIn) {
        this.itemStack = itemStackIn;
    }

    /**
     * Returns true if the specified ItemStack exists in the inventory.
     */
    public boolean hasItemStack(ItemStack itemStackIn) {
        label23:
        for (List<ItemStack> list : this.allInventories) {
            Iterator iterator = list.iterator();

            while (true) {
                if (!iterator.hasNext()) {
                    continue label23;
                }

                ItemStack itemstack = (ItemStack) iterator.next();
                if (!itemstack.isEmpty() && itemstack.sameItem(itemStackIn)) {
                    break;
                }
            }

            return true;
        }

        return false;
    }

    public ItemStack getFood() {
        label23:
        for (List<ItemStack> list : this.allInventories) {
            Iterator iterator = list.iterator();

            while (true) {
                if (!iterator.hasNext()) {
                    continue label23;
                }

                ItemStack itemstack = (ItemStack) iterator.next();
                if (!itemstack.isEmpty() && itemstack.getItem().isEdible()) {
                    return itemstack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasTag(Tag<Item> itemTag) {
        label23:
        for (List<ItemStack> list : this.allInventories) {
            Iterator iterator = list.iterator();

            while (true) {
                if (!iterator.hasNext()) {
                    continue label23;
                }

                ItemStack itemstack = (ItemStack) iterator.next();
                if (!itemstack.isEmpty() && itemTag.contains(itemstack.getItem())) {
                    break;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Copy the ItemStack contents from another Inventorysim instance
     */
    public void copyInventory(SimInventory simInventory) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, simInventory.getItem(i));
        }

        this.currentItem = simInventory.currentItem;
    }

    public void clearContent() {
        for (List<ItemStack> list : this.allInventories) {
            list.clear();
        }

    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player) {
        return new SimContainer(i, player.level.isClientSide(), sim, playerInventory);
    }

    public IItemHandlerModifiable getHandler() {
        if (handler == null)
            handler = new CombinedInvWrapper(new InvWrapper(this), new SimArmorInvWrapper(this), new SimOffHandInvWrapper(this));
        return handler;
    }

    public SimEntity getSim() {
        return sim;
    }

    @Override
    public ITextComponent getDisplayName() {
        if (!inventoryTitle.isEmpty())
            return new StringTextComponent(inventoryTitle);
        else return new StringTextComponent("Sim");
    }

    public void setCustomName(String name) {
        this.hasCustomName = true;
        this.inventoryTitle = name;
    }
}
