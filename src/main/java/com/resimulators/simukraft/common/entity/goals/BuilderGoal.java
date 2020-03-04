package com.resimulators.simukraft.common.entity.goals;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.jobs.core.EnumJobState;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.BreakBlockGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3i;

public class BuilderGoal extends Goal {
    private EntitySim sim;
    private int tick;
    BuilderGoal(EntitySim sim){
        this.sim = sim;

    }
    @Override
    public boolean shouldExecute() {
        IJob job = sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK){
            if (sim.getPosition().withinDistance(new Vec3i(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)){
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
