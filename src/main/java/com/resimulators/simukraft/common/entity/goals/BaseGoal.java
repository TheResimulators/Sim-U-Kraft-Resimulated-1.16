package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;

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

    @Override
    public void resetTask() {
        super.resetTask();
        sim.setStatus("");
    }

    public ArrayList<BlockPos> findChestAroundTargetBlock(BlockPos targetBlock, int distance, World world) {
        ArrayList<BlockPos> blockPoses = BlockUtils.getBlocksAroundAndBelowPosition(targetBlock, distance);
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (BlockPos blockPos : blockPoses) {
            if (world.getTileEntity(blockPos) instanceof ChestTileEntity) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }
}
