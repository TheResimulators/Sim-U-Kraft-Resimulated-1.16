package com.resimulators.simukraft.common.jobs.core;

import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

public interface IJob {

    EnumJobState state();

    String name();
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


}


