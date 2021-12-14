package com.resimulators.simukraft.common.entity.pathfinding;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.block.Block;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public class InteractGateGoal extends Goal {
    /**
     * Our citizen
     */
    protected SimEntity sim;
    /**
     * The gate position
     */
    protected BlockPos gatePosition;
    /**
     * The gate block
     */
    protected FenceGateBlock gateBlock;
    /**
     * Check if the interaction with the fenceGate stopped already.
     */
    private boolean hasStoppedFenceInteraction;
    /**
     * The entities x and z position
     */
    private float entityPositionX;
    private float entityPositionZ;

    /**
     * Constructor called to register the AI class with an entity
     * @param entityIn the registering entity
     */
    public InteractGateGoal(SimEntity entityIn) {
        this.gatePosition = BlockPos.ZERO;
        this.sim = entityIn;
        if (!(entityIn.getNavigation() instanceof GroundPathNavigator)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    /**
     * Checks if the Interaction should be executed
     * @return true or false depending on the conditions
     */
    @Override
    public boolean canUse() {
        if (!this.sim.horizontalCollision) {
            return false;
        } else {
            GroundPathNavigator pathnavigateground = (GroundPathNavigator) sim.getNavigation();
            Path pathentity = pathnavigateground.getPath();
            if (pathentity != null && !pathentity.isDone() && pathnavigateground.canOpenDoors()) {
                for (int i = 0; i < Math.min(pathentity.getNextNodeIndex() + 2, pathentity.getNodeCount()); ++i) {
                    PathPoint pathpoint = pathentity.getNode(i);
                    this.gatePosition = new BlockPos(pathpoint.x, pathpoint.y + 0, pathpoint.z);
                    if (this.sim.blockPosition().distSqr(gatePosition) <= 2.25D) {
                        this.gateBlock = this.getBlockFence(this.gatePosition);
                        if (this.gateBlock != null) {
                            return true;
                        }
                    }
                }

                this.gatePosition = sim.blockPosition().above();
                this.gateBlock = this.getBlockFence(this.gatePosition);
                return this.gateBlock != null;
            } else {
                return false;
            }
        }
    }

    /**
     * Checks if the execution is still ongoing
     * @return true or false
     */
    @Override
    public boolean canContinueToUse() {
        return !this.hasStoppedFenceInteraction;
    }

    /**
     * Starts the execution
     */
    @Override
    public void start() {
        this.hasStoppedFenceInteraction = false;
        this.entityPositionX = (float) ((double) ((float) this.gatePosition.getX() + 0.5F) - this.sim.getX());
        this.entityPositionZ = (float) ((double) ((float) this.gatePosition.getZ() + 0.5F) - this.sim.getZ());
    }

    /**
     * Updates the task
     */
    @Override
    public void tick() {
        float f = (float) ((double) ((float) this.gatePosition.getX() + 0.5F) - this.sim.getX());
        float f1 = (float) ((double) ((float) this.gatePosition.getZ() + 0.5F) - this.sim.getZ());
        float f2 = this.entityPositionX * f + this.entityPositionZ * f1;
        if (f2 < 0.0F) {
            this.hasStoppedFenceInteraction = true;
        }

    }

    /**
     * Returns a fenceBlock if available
     * @param pos the position to be searched
     * @return fenceBlock or null
     */
    private FenceGateBlock getBlockFence(BlockPos pos)
    {
        Block block  = this.sim.level.getBlockState(pos).getBlock();
        if(!(block instanceof FenceGateBlock && block.defaultBlockState().getMaterial() == Material.WOOD))
        {
            block = this.sim.level.getBlockState(this.sim.blockPosition()).getBlock();
            gatePosition = this.sim.blockPosition();
        }
        return (block instanceof FenceGateBlock && block.defaultBlockState().getMaterial() == Material.WOOD ? (FenceGateBlock) block : null);
    }
}


