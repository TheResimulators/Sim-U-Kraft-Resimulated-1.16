package com.resimulators.simukraft.common.entity.sim;


import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;


public class WorkingController {
    private SimEntity sim;
    private int tick;

    public WorkingController(SimEntity sim) {
        this.sim = sim;
    }


    public void tick() {
        IJob job = sim.getJob();
        if (job != null){
        if (job.getState() == EnumJobState.NOT_WORKING) {// only runs this if the sim is not working at all
            if (tick >= job.intervalTime()) { //interval time makes it so it checks every x seconds to see if it can work
                tick = 0;
                //TODO add if idling condition to make sure that we don't interrupt anything else like eating or socializing add to the one below

                if (job.getPeriodsWorked() < job.maximumWorkPeriods()) {
                    if (sim.getEntityWorld().isDaytime() || job.nightShift()) {
                        if (job.getWorkSpace() != null) {
                            job.setState(EnumJobState.GOING_TO_WORK);
                            BlockPos pos = new BlockPos(job.getWorkSpace());
                            sim.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), sim.getAIMoveSpeed()*2);
                            }
                        }
                    }
                }
            } else if (job.getState() == EnumJobState.GOING_TO_WORK){
            BlockPos pos = new BlockPos(job.getWorkSpace());
            sim.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), sim.getAIMoveSpeed()*2);
        } else {
            if (!sim.world.isDaytime()) {
                if (!job.nightShift()) {
                    job.setState(EnumJobState.FORCE_STOP);
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
