package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class TalkingToPlayerGoal extends Goal {
    private final SimEntity sim;

    public TalkingToPlayerGoal(SimEntity sim) {
        this.sim = sim;
        this.setMutexFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    public boolean shouldExecute() {
        if (!this.sim.isAlive()) {
            return false;
        } else if (this.sim.isInWater()) {
            return false;
        } else if (!this.sim.func_233570_aj_()) {
            return false;
        } else if (this.sim.velocityChanged) {
            return false;
        } else {
            PlayerEntity player = this.sim.getInteractingPlayer();
            if (player == null) {
                return false;
            } else if (this.sim.getDistanceSq(player) > 16) {
                return false;
            } else {
                return player.openContainer != null;
            }
        }
    }

    @Override
    public void startExecuting() {
        this.sim.getNavigator().clearPath();
    }

    @Override
    public void resetTask() {
        this.sim.setInteractingPlayer(null);
    }
}
