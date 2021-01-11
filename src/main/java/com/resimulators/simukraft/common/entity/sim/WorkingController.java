package com.resimulators.simukraft.common.entity.sim;


import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;


public class WorkingController {
    private SimEntity sim;
    private int tick;

    public WorkingController(SimEntity sim) {
        this.sim = sim;
    }


    public void tick() {
        IJob job = sim.getJob();
        if (job != null){
        if (sim.getActivity() != Activity.WORKING  && sim.getActivity() != Activity.GOING_TO_WORK ) {// only runs this if the sim is not working at all
            if (tick >= job.intervalTime()) { //interval time makes it so it checks every x seconds to see if it can work
                tick = 0;
                //TODO add if idling condition to make sure that we don't interrupt anything else like eating or socializing add to the one below

                if (job.getPeriodsWorked() < job.maximumWorkPeriods() || job.maximumWorkPeriods() < 0) {
                    if (sim.getEntityWorld().isDaytime() || job.nightShift()) {
                        if (job.getWorkSpace() != null) {
                            sim.setActivity(Activity.GOING_TO_WORK);

                            BlockPos pos = new BlockPos(job.getWorkSpace());
                            sim.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), sim.getAIMoveSpeed()*2);
                            }
                        }
                    }
                }
            } else if (sim.getActivity() == Activity.GOING_TO_WORK){
            BlockPos pos = new BlockPos(job.getWorkSpace());
            sim.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), sim.getAIMoveSpeed()*2);
        } else {
            if (!sim.world.isDaytime()) {
                if (!job.nightShift()) {
                    sim.setActivity(Activity.FORCE_STOP);
                    }
                }
            }
        }
        tick++;
    }


    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("tick", tick);
        return nbt;
    }


    public void deserializeNBT(CompoundNBT nbt) {
        tick = nbt.getInt("tick");
    }

    public void setTick(int tick){
        this.tick = tick;
    }

    public int getTick(){
        return tick;
    }



}
