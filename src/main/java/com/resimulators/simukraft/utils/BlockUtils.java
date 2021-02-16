package com.resimulators.simukraft.utils;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockUtils {
    public static ArrayList<BlockPos> getBlocksAroundPosition(BlockPos startingPos, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos blockPos = startingPos.add(x, 0, z);
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
    public static ArrayList<BlockPos> getBlocksAroundAndBelowPosition(BlockPos startingPos, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int y = -1; y <=1;y++){
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = startingPos.add(x, y, z);
                    blocks.add(blockPos);
                }
            }
        }

        return blocks;
    }
    public static boolean aboveBlocksValid(World world, BlockPos startingPos) {
        for (int y = 0; y <= 3; y++) {
            BlockPos blockAbovePos = startingPos.add(0, y, 0);
            BlockState blockAboveState = world.getBlockState(blockAbovePos);
            if (blockAboveState.isSolid()) return false;
        }
        return true;
    }

    public static BlockPos getGroundBlock(World world, BlockPos startingPos) {
        BlockPos newPos = startingPos;
        if (isBlockSolid(world, newPos)) return newPos.up();
        if (isBlockAboveSolid(world, newPos)) {
            // Keep going up until they don't have a block above anymore
            do {
                newPos = newPos.up();
            } while (isBlockAboveSolid(world, newPos));
            return newPos;
        }
        if (!isBlockBelowSolid(world, newPos)) {
            // Keep going down until it reaches a block
            do {
                newPos = newPos.down();
            } while (!isBlockBelowSolid(world, newPos));
            return newPos;
        }
        return newPos;
    }

    private static boolean isBlockSolid(World world, BlockPos pos) {
        return world.getBlockState(pos).isSolid();
    }

    private static boolean isBlockAboveSolid(World world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSolid();
    }

    private static boolean isBlockBelowSolid(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSolid();
    }

    public static boolean blocksAreValid(World world, ArrayList<BlockPos> blocksPos, SimEntity sim) {
        for (BlockPos blockPos : blocksPos)
            if (!BlockUtils.blockIsValid(world, blockPos, sim)) return false;
        return true;
    }

    public static boolean blockIsValid(World world, BlockPos blockPos, SimEntity sim) {
        if (!aboveBlocksValid(world, blockPos)) {
            SimuKraft.LOGGER().debug("Above blocks not valid");
            return false;
        }
        return true;
    }
}
