package com.resimulators.simukraft.utils;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {
    private static final Random rand = new Random();

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

    public static int getReversedInt(int size, int index) {
        return size - index - 1;
    }

    public static boolean addSimInventoryToChest(ChestTileEntity chest, SimEntity sim) {
        InvWrapper wrapper = new InvWrapper((chest));
        for (int i = 0; i < sim.getInventory().mainInventory.size(); i++) {
            ItemStack stack = sim.getInventory().mainInventory.get(i);
            if (!stack.equals(ItemStack.EMPTY) && !(stack.getItem() instanceof ToolItem)) {
                if (ItemHandlerHelper.insertItemStacked(wrapper,stack,false) != ItemStack.EMPTY){
                    SimuKraft.LOGGER().debug("No Room in chest");
                    return false;
                }
            }
        }
        return true;
    }

    public static int findNextAvaliableSlot(ChestTileEntity chest) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack == ItemStack.EMPTY) {
                return i;
            }
        }
        return -1;
    }

    public static ChestTileEntity getInventoryAroundPos(BlockPos pos, World world) {
        int range = 6;
        pos = pos.offset(-range / 2, 0, -range / 2);
        BlockPos current = pos;
        for (int i = 0; i < range; i++) {
            for (int j = 0; j < range; j++) {
                current = pos.offset(i, 0, j);
                TileEntity entity = world.getBlockEntity(current);
                if (entity instanceof ChestTileEntity) {
                    return (ChestTileEntity) entity;
                }
            }
        }
        return null;
    }

    public static ArrayList<BlockPos> findInventoriesAroundPos(BlockPos targetBlock, int distance, World world) {
        ArrayList<BlockPos> blockPoses = BlockUtils.getBlocksAroundAndBelowPosition(targetBlock, distance);
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (BlockPos blockPos : blockPoses) {
            if (world.getBlockEntity(blockPos) instanceof ChestTileEntity) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
}
