package com.resimulators.simukraft.common.jobs;

import com.resimulators.simukraft.common.entity.goals.GlassFactoryGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

public class JobGlassFactory implements IJob {
    private final SimEntity sim;
    private final Goal goal1;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private Activity state = Activity.NOT_WORKING;
    private boolean finished;


    public JobGlassFactory(SimEntity simEntity) {
        this.sim = simEntity;
        goal1 = new GlassFactoryGoal(sim);
        addJobAi();
    }

    @Override
    public Activity getState() {
        return state;
    }

    @Override
    public void setState(Activity state) {
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
        sim.goalSelector.addGoal(3, goal1);
    }

    @Override
    public void removeJobAi() {
        sim.goalSelector.removeGoal(goal1);
    }

    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT data = new CompoundNBT();
        data.putInt("id", sim.getProfession());
        nbt.add(data);
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.asLong());
        }
        nbt.add(other);
        other.putBoolean("finished", finished);
        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsworked = list.getInt("periodsworked");
            }
            if (list.contains("jobpos")) {
                setWorkSpace(BlockPos.of(list.getLong("jobpos")));
            }
            if (list.contains("finished")) {
                finished = list.getBoolean("finished");
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
    public BlockPos getWorkSpace() {
        return workSpace;
    }

    @Override
    public void setWorkSpace(BlockPos pos) {
        this.workSpace = pos;
    }

    @Override
    public boolean hasAiRunning() {
        return sim.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() == goal1);
    }

    @Override
    public double getWage() {
        return 0.7d;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
