package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class GoToWorkGoal extends MoveToBlockGoal {

    SimEntity sim;
     IJob job;

    public GoToWorkGoal(SimEntity sim){
        super(sim,sim.getAIMoveSpeed()*2,20);
        this.sim = sim;

    }
    @Override
    public boolean shouldExecute() {
        if (sim.getJob() == null)return false;
        if (sim.getJob().getState() == EnumJobState.GOING_TO_WORK){
            this.job = sim.getJob();
            return true;
        }


        return false;
    }


    @Override
    public void startExecuting(){

        destinationBlock = sim.getJob().getWorkSpace();
        func_220725_g();
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return pos.withinDistance(sim.getPositionVec(),getTargetDistanceSq());
    }

    @Override
    public void tick() {
        super.tick();
    }
}
