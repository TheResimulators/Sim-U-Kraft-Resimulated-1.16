package com.Resimulators.simukraft.common.jobs.core;

import com.Resimulators.simukraft.common.jobs.core.EnumJobState;

public interface IJob {
    EnumJobState state();
    //default work periods and intervals is 2 a day 4 hours (in game) of work
    //2 hours rest and another 4 hours.

    //interval of ticks between work periods,
    int intervaltime();
    //how many ticks to work for each time they work period
    int workTime();
    //maximum time a sim can go to work
    int maximumworkperiods();

    //should they work at night
    boolean nightShift();

    //add the jobs ai to the sim. allows minimal amount of ai's to be active in a sim at once,
    // should be called when the sim is hired
    void addJobAi();

    //removes unneeded job Ai when the sim is fired
    void removeJobAi();


}
