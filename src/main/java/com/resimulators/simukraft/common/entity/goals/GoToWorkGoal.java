package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class GoToWorkGoal extends MoveToBlockGoal {

    SimEntity sim;
    IReworkedJob job;

    public GoToWorkGoal(SimEntity sim) {
        super(sim, sim.getSpeed() * 2, 20);
        this.sim = sim;

    }

    @Override
    public boolean canUse() {
        if (sim.getJob() == null) return false;
        if (sim.getJob().getActivity() == Activity.GOING_TO_WORK) {
            this.job = sim.getJob();
            return true;
        }


        return false;
    }


    @Override
    public void start() {

        blockPos = sim.getJob().getWorkSpace();
        moveMobToBlock();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected boolean isValidTarget(IWorldReader worldIn, BlockPos pos) {
        return pos.closerThan(sim.position(), acceptedDistance());
    }
}
