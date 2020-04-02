package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.EntitySim;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Direction;
import net.minecraft.util.datafix.fixes.StringToUUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class MinerGoal extends Goal {
    private EntitySim sim;
    private int tick;
    private IJob job;
    private BlockPos markerPos;
    private BlockPos offset;
    private int width;
    private int depth;
    private Direction dir;

    private int progress;

    public MinerGoal(EntitySim sim) {
        job = sim.getJob();
        this.sim = sim;
    }

    @Override
    public boolean shouldExecute() {

        job = sim.getJob();
        if (job == null) return false;
        if (job.getWorkSpace() == null) return false;
        if (((TileMiner) sim.world.getTileEntity(job.getWorkSpace())).getMarker() == null) return false;
        if (!((TileMiner) sim.world.getTileEntity(job.getWorkSpace())).CheckValidity()) return false;
        if (job.getWorkSpace() == null) return false;
        if (sim.world.getTileEntity(job.getWorkSpace()) == null) return false;
        if (true) return true;
        if (job.getState() == EnumJobState.GOING_TO_WORK) {
            if (sim.getPosition().withinDistance(new Vec3i(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                job.setState(EnumJobState.WORKING);
                return true;
            }
        }


        return false;
    }


    @Override
    public void startExecuting() {
        TileMiner miner = ((TileMiner) sim.world.getTileEntity(job.getWorkSpace()));
        progress = ((JobMiner) sim.getJob()).getProgress();
        markerPos = miner.getMarker();
        dir = miner.getDir();
        offset = BlockPos.ZERO.offset(dir).offset(dir.rotateY());
        width = miner.getWidth() - 1;
        depth = miner.getDepth() - 1;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                sim.world.setBlockState(markerPos.add(offset.getX(), offset.getY() + 4, offset.getZ()).offset(dir, z).offset(dir.rotateY(), x), Blocks.COBBLESTONE.getDefaultState());
                BlockPos pos = markerPos.add(offset.getX(), offset.getY() + 4, offset.getZ()).offset(dir, z).offset(dir.rotateY(), x);
                System.out.println(pos);
            }
        }
    }

    @Override
    public void tick() {
        tick++;


    }


    @Override
    public boolean shouldContinueExecuting() {
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == EnumJobState.FORCE_STOP) {
                ((JobMiner) job).setProgress(progress);
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            } else {
                sim.getJob().finishedWorkPeriod();
                sim.getJob().setState(EnumJobState.NOT_WORKING);
            }
        }
        ((JobMiner) job).setProgress(progress);
        return false;
    }

}


