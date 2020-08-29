package com.resimulators.simukraft.common.jobs.core;

import com.resimulators.simukraft.common.jobs.Profession;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

public interface IJob {

    EnumJobState getState();

    //set the state that the job is in. i.e when the sim starts to work set it to going to work. then the ai can use it to determine what to do when etc.
    void setState(EnumJobState state);

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
    
    void resetPeriodsWorked();

    void setWorkSpace(BlockPos pos);

    BlockPos getWorkSpace();

    boolean hasAi();


}


