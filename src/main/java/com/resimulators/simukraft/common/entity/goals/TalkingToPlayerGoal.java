package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class TalkingToPlayerGoal extends Goal {
    private final SimEntity sim;

    public TalkingToPlayerGoal(SimEntity sim) {
        this.sim = sim;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    public boolean canUse() {
        if (!this.sim.isAlive()) {
            return false;
        } else if (this.sim.isInWater()) {
            return false;
        } else if (!this.sim.isOnGround()) {
            return false;
        } else if (this.sim.hurtMarked) {
            return false;
        } else {
            PlayerEntity player = this.sim.getInteractingPlayer();
            if (player == null) {
                return false;
            } else if (this.sim.distanceToSqr(player) > 16) {
                return false;
            } else {
                return player.containerMenu != null;
            }
        }
    }

    @Override
    public void start() {
        this.sim.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.sim.setInteractingPlayer(null);
    }
}
