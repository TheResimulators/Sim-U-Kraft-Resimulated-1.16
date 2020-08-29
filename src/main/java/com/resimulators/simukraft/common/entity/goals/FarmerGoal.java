package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;

public class FarmerGoal extends MoveToBlockGoal {
    private SimEntity sim;
    private final World world;
    private int tick;
    private IJob job;
    private int delay = 20;
    private BlockPos targetPos;
    private State state;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private TileMarker marker;
    private TileFarmer farmerTile;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int layer = 0; // used as what current depth the

    public FarmerGoal(SimEntity sim) {
        super(sim, 2*sim.getAIMoveSpeed(), 20);
        this.sim = sim;
        this.world = sim.getEntityWorld();
    }
    @Override
    public boolean shouldExecute() {
        job = sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK){
            if (sim.func_233580_cy_().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)) {
                job.setState(EnumJobState.WORKING);
                return true;
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(), Items.DIAMOND_HOE.getDefaultInstance());
        state = State.RESOURCES;
        farmerTile = (TileFarmer) world.getTileEntity(job.getWorkSpace());

    }

    @Override
    public void tick() {
        super.tick();

    }


    @Override
    public double getTargetDistanceSq() {
        return 2.0d;
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

    private boolean hasSeeds(){
        for (BlockPos pos: chests){

        }

        return false;
    }

    private void findChests(){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundPosition(job.getWorkSpace(),5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
    }




    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return sim.getDistanceSq(pos.getX(),pos.getY(),pos.getZ()) < getTargetDistanceSq();
    }




    private enum State{
        RESOURCES,
        TRAVELING,
        PLANTING,
        HARVESTING,
        RETURNING,


    }
}
