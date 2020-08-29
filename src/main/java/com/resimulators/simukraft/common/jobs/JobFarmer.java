package com.resimulators.simukraft.common.jobs;

import com.resimulators.simukraft.common.entity.goals.FarmerGoal;
import com.resimulators.simukraft.common.entity.goals.MinerGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class JobFarmer implements IJob {
    private SimEntity sim;
    private Goal goal1;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private EnumJobState state = EnumJobState.NOT_WORKING;

    public JobFarmer(SimEntity sim) {
        this.sim = sim;
        goal1 = new FarmerGoal(sim);
        addJobAi();

    }


    @Override
    public Profession jobType() {
        return Profession.FARMER;
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
    public int intervalTime() {
        return 1000;
    }

    @Override
    public int workTime() {
        return 10000;
    }

    @Override
    public int maximumWorkPeriods() {
        return -1;
        //negative one so that it can work as much as it can. builder should work all day.
        // if it can't find resources it take a 1000 tick break
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
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.toLong());
        }
        nbt.add(other);

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
                setWorkSpace(BlockPos.fromLong(list.getLong("jobpos")));
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

    @Override
    public boolean hasAi() {
        return sim.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() == goal1);
    }
}


