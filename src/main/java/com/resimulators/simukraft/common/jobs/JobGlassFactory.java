package com.resimulators.simukraft.common.jobs;

import com.resimulators.simukraft.common.entity.goals.GlassFactoryGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

public class JobGlassFactory implements IJob {
    private SimEntity sim;
    private Goal goal1;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private EnumJobState state = EnumJobState.NOT_WORKING;


    public JobGlassFactory(SimEntity simEntity) {
        this.sim = simEntity;
        goal1 = new GlassFactoryGoal(sim);
        addJobAi();
    }

    @Override
    public EnumJobState getState() {
        return state;
    }

    @Override
    public void setState(EnumJobState state) {
        this.state = state;
    }

    @Override
    public Profession jobType() {
        return Profession.GLASS_FACTORY;
    }

    @Override
    public int intervalTime() {
        return 400;
    }

    @Override
    public int workTime() {
        return 10000;
    }

    @Override
    public int maximumWorkPeriods() {
        return 3;
    }

    @Override
    public boolean nightShift() {
        return false;
    }

    @Override
    public int getPeriodsWorked() {
        return periodsworked;
    }

    @Override
    public void addJobAi() {
        sim.goalSelector.addGoal(4,goal1);
    }

    @Override
    public void removeJobAi() {
        sim.goalSelector.removeGoal(goal1);
    }

    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);

        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsworked = list.getInt("periodsworked");
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(++periodsworked);
    }

    @Override
    public void setWorkedPeriods(int periods) {
        periodsworked = periods;
    }

    @Override
    public void resetPeriodsWorked() {
        setWorkedPeriods(0);
    }

    @Override
    public void setWorkSpace(BlockPos pos) {
        this.workSpace = pos;
    }

    @Override
    public BlockPos getWorkSpace() {
        return workSpace;
    }
}
