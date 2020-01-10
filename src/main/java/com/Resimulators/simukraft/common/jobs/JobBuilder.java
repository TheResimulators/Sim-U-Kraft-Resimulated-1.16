package com.Resimulators.simukraft.common.jobs;

import com.Resimulators.simukraft.common.jobs.core.EnumJobState;
import com.Resimulators.simukraft.common.jobs.core.IJob;

public class JobBuilder implements IJob {
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

    }

    @Override
    public void removeJobAi() {

    }
}
