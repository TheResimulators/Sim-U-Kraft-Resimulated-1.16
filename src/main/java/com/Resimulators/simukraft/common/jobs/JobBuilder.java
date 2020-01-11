package com.Resimulators.simukraft.common.jobs;

import com.Resimulators.simukraft.common.entity.EntitySim;
import com.Resimulators.simukraft.common.jobs.core.EnumJobState;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveTowardsTargetGoal;

public class JobBuilder implements IJob {
    private EntitySim sim;
    private Goal goal1;
    JobBuilder(EntitySim sim){
        this.sim = sim;

    }
    @Override
    public EnumJobState state() {
        return EnumJobState.NOT_WORKING;
    }

    @Override
    public int intervaltime() {
        return 2000;
    }

    @Override
    public int workTime() {
        return 4000;
    }

    @Override
    public int maximumworkperiods() {
        return 2;
    }

    @Override
    public boolean nightShift() {
        return false;
    }

    @Override
    public void addJobAi() {
        sim.goalSelector.addGoal(4,goal1);
    }

    @Override
    public void removeJobAi() {
        sim.goalSelector.removeGoal(goal1);
    }
}
