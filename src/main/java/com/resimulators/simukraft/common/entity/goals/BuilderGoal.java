package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.vector.Vector3d;

public class BuilderGoal extends Goal {
    private SimEntity sim;
    private int tick;
    BuilderGoal(SimEntity sim){
        this.sim = sim;

    }
    @Override
    public boolean shouldExecute() {
        IJob job = sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK){
            if (sim.func_233580_cy_().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)){
                job.setState(EnumJobState.WORKING);
                return true;
            }
        }




        return false;
    }


    @Override
    public void startExecuting(){
    //done the condition checking for it starting just need the rest done
    //TODO the Builder AI
    //FABBE50

    }

    @Override
    public void tick() {
        tick++;

    }
    @Override
    public boolean shouldContinueExecuting(){
        if (sim.getJob().getState() == EnumJobState.FORCE_STOP){
            return false;
        }
        if (tick<sim.getJob().workTime()){
            return true;
        }else{
            sim.getJob().finishedWorkPeriod();
            sim.getJob().setState(EnumJobState.NOT_WORKING);
        }

        return false;
    }
}
