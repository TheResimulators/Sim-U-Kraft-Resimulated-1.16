package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;

public class GlassFactoryGoal extends MoveToBlockGoal {
    private final SimEntity sim;
    World world;
    private int tick;
    private IJob job;
    private State state = State.NOTHING;
    private boolean collected = false;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private ArrayList<BlockPos> furnaces = new ArrayList<>();

    public GlassFactoryGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
        job = sim.getJob();
        this.sim = sim;
        this.world = sim.world;

    }




    @Override
    public boolean shouldExecute() {
        IJob job = sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK){
            if (sim.func_233580_cy_().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)){
                job.setState(EnumJobState.WORKING);
                if (!findChestAroundBlock(job.getWorkSpace())){
                    SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage("Sim (Glass Factory) has no Inventory at " + job.getWorkSpace(), world);
                    return false;
                }
                if (!findFurnaceAroundBlock(job.getWorkSpace())){
                    SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage("Sim (Glass Factory) Has no Furnaces to smelt with at " + job.getWorkSpace(), world);
                    return false;
                }


                return true;
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {

    }


    @Override
    public void tick() {
        super.tick();

    }

    @Override
    public double getTargetDistanceSq() {
        return super.getTargetDistanceSq();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == EnumJobState.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            }
        }
        return shouldExecute();
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return false;
    }


    public boolean findChestAroundBlock(BlockPos workPos){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
        return !chests.isEmpty();
    }

    public boolean findFurnaceAroundBlock(BlockPos workPos){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof FurnaceTileEntity){
                furnaces.add(pos);
            }
        }
        return !furnaces.isEmpty();
    }


    private enum State {
        TRAVELING,
        COLLECTING,
        SMELTING,
        WAITING,
        NOTHING


    }
}
