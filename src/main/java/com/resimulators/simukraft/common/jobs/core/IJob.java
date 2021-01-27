package com.resimulators.simukraft.common.jobs.core;

import com.resimulators.simukraft.common.jobs.Profession;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

public interface IJob {
    //gets the state the sim is in, used to see if they should work or not
    Activity getState();

    //set the state that the job is in. i.e when the sim starts to work set it to going to work. then the ai can use it to determine what to do when etc.
    void setState(Activity state);
    // what profession is this job for
    Profession jobType();
    //interval of ticks between work periods,
    int intervalTime();
    //how many ticks to work for each time they work period
    int workTime();
    //maximum time a sim can go to work
    int maximumWorkPeriods();

    //should they work at night
    boolean nightShift();

    int getPeriodsWorked();
    //add the jobs ai to the sim. allows minimal amount of ai's to be active in a sim at once,
    // should be called when the sim is hired
    void addJobAi();

    //removes unneeded job Ai when the sim is fired
    void removeJobAi();

    ListNBT writeToNbt(ListNBT nbt);

    void readFromNbt(ListNBT nbt);
    
    void finishedWorkPeriod();
    
    void setWorkedPeriods(int periods);
    //resets work periods so the sim can work more
    void resetPeriodsWorked();

    void setWorkSpace(BlockPos pos);

    BlockPos getWorkSpace();

    boolean hasAiRunning();
    //gets the amount the job gets paid per x Ticks
    double getWage();

    boolean isFinished();

    void setFinished(boolean finished);


}


