package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BaseGoal<JobType extends IJob>  extends MoveToBlockGoal{
    JobType job;
    protected SimEntity sim;
    private int delay = 60; // every 3 seconds (60 ticks) pay sim their wage
    public BaseGoal(SimEntity sim, double speedIn, int length) {
        super(sim, speedIn, length);
        this.sim = sim;
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return false;
    }


    @Override
    public void tick() {
        super.tick();
        if (delay <= 0){
            delay = 60;
            Faction faction = SavedWorldData.get(sim.getEntityWorld()).getFactionWithSim(sim.getUniqueID());
            if(faction != null){
                if (faction.hasEnoughCredits(job.getWage())){
                    faction.subCredits(job.getWage());
                }
            }
        }else {
            delay--;
        }
    }


    @Override
    public boolean shouldContinueExecuting() {
        Faction faction = SavedWorldData.get(sim.getEntityWorld()).getFactionWithSim(sim.getUniqueID());
        if (faction != null) {
            return faction.hasEnoughCredits(job.getWage());
        }
        return false;
    }
}
