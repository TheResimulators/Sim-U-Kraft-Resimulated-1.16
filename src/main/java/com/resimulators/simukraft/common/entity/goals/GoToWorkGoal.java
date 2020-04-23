package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.EntitySim;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class GoToWorkGoal extends Goal {

    EntitySim sim;
     IJob job;

    public GoToWorkGoal(EntitySim sim){
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
        BlockPos workSpace = job.getWorkSpace();
        sim.getNavigator().tryMoveToXYZ(workSpace.getX(),workSpace.getY(),workSpace.getZ(),sim.getAIMoveSpeed());

    }

    @Override
    public void tick(){
        if (sim.getNavigator().noPath()){
            BlockPos workSpace = job.getWorkSpace();
            sim.getNavigator().tryMoveToXYZ(workSpace.getX(),workSpace.getY(),workSpace.getZ(),sim.getAIMoveSpeed());
        }
    }
}
