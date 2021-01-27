package com.resimulators.simukraft.common.jobs;

import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.goals.BuilderGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;


public class JobBuilder implements IJob {
    private final SimEntity sim;
    private final Goal goal1;
    private BuildingTemplate template;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private Activity state = Activity.NOT_WORKING;
    private Direction direction;
    private boolean finished;

    public JobBuilder(SimEntity sim) {
        this.sim = sim;
        goal1 = new BuilderGoal(sim);
        addJobAi();
    }

    public void setTemplate(BuildingTemplate template) {
        this.template = template;
    }

    public BuildingTemplate getTemplate() {
        return template;
    }

    public void setDirection(Direction dir){
        this.direction = dir;
    }

    public Direction getDirection(){
        return direction;
    }
    @Override
    public Profession jobType() {
        return Profession.BUILDER;
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
    public int intervalTime() {
        return 200;
    }

    @Override
    public int workTime() {
        return 12000;
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
        System.out.println("Added " + goal1 + " to sim " + sim.getUniqueID());
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
            other.putLong("jobpos", workSpace.toLong());
        }
        other.putBoolean("finished",finished);
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
            if (list.contains("finished")){
                finished = list.getBoolean("finished");
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(periodsworked++);
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
    public boolean hasAiRunning()  {
        return sim.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() == goal1);
    }

    @Override
    public double getWage() {
        return 0;
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
