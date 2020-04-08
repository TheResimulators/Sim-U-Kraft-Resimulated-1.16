package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.entity.ai.goal.Goal;

public class GoToWorkGoal extends Goal {

    EntitySim sim;


    public GoToWorkGoal(EntitySim sim){
        
    }
    @Override
    public boolean shouldExecute() {
        return false;
    }


    @Override
    public void startExecuting(){}

    @Override
    public void tick(){

    }
}
