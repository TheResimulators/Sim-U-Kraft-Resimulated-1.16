package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobAnimalFarmer;

public class AnimalFarmerGoal extends BaseGoal<JobAnimalFarmer>{
    public AnimalFarmerGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
    }
}
