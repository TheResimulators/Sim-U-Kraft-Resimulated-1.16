package com.resimulators.simukraft.common.entity.pathfinding;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class OpenGateGoal extends InteractGateGoal {

    /**
     * Checks if the gate should be closed
     */
    private boolean closeDoor;
    /**
     * Ticks until the gate should be closed
     */
    private int closeDoorTemporisation;

    /**
     * Constructor called to register the AI class with an entity
     * @param sim the registering entity
     * @param shouldClose should the entity close the gate?
     */
    public OpenGateGoal(SimEntity sim, boolean shouldClose) {
        super(sim);
        this.sim = sim;
        this.closeDoor = shouldClose;
    }

    /**
     * Should the AI continue to execute?
     * @return true or false
     */
    @Override
    public boolean canContinueToUse() {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.canContinueToUse();
    }

    /**
     * Start the execution
     */
    @Override
    public void start() {
        this.closeDoorTemporisation = 20;
        toggleGate(true);
    }

    /**
     * runs on stopping the action
     */
    @Override
    public void stop() {
        if (this.closeDoor) {
            toggleGate(false);
        }
    }

    /**
     * Toggles the gate(Opens or closes)
     * @param open if open or close
     */
    private void toggleGate(boolean open) {
        BlockState iblockstate = this.sim.level.getBlockState(this.gatePosition);
        if (iblockstate.getBlock() == this.gateBlock) {
            if ((iblockstate.getValue(FenceGateBlock.OPEN)) != open) {
                this.sim.level.setBlock(this.gatePosition, iblockstate.getBlockState().setValue(FenceGateBlock.OPEN, open), 2);
                this.sim.level.playSound(null, gatePosition, open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    /**
     * Updates the task.
     */
    public void tick() {
        --this.closeDoorTemporisation;
        super.tick();
    }

    public void setCloseDoor(boolean closeDoor){
        this.closeDoor = closeDoor;
    }
}