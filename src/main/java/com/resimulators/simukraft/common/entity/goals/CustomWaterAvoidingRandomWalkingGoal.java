package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.Activity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;

import java.util.ArrayList;

public class CustomWaterAvoidingRandomWalkingGoal extends WaterAvoidingRandomWalkingGoal {

    private SimEntity sim;

    private final ArrayList<Activity> activitiesToWander = new ArrayList<Activity>(){
        {
            add(Activity.WANDERING);
            add(Activity.IDLING);
            add(Activity.NOT_WORKING);
        }
    };

    public CustomWaterAvoidingRandomWalkingGoal(SimEntity sim, double speed) {
        super(sim, speed);
        this.sim = sim;
    }


    @Override
    public boolean canUse(){
        if (activitiesToWander.contains(sim.getActivity())){
            super.canUse();
        }
        return false;


    }
}
