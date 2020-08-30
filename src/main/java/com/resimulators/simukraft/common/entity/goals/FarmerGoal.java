package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.entity.sim.SimInventory;
import com.resimulators.simukraft.common.jobs.JobFarmer;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
                if (hasSeeds()){
                job.setState(EnumJobState.WORKING);
                return true;
                }
            else {
                    SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage((sim.getDisplayName().getString() + " does not have the seeds required to work" + "("+farmerTile.getSeed().getName() + ")")  ,world);
                }
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(), Items.DIAMOND_HOE.getDefaultInstance());
        state = State.RESOURCES;
        farmerTile = (TileFarmer) world.getTileEntity(job.getWorkSpace());
        findChests();

    }

    @Override
    public void tick() {
        super.tick();
        if (delay <= 0){
        if (state == State.RESOURCES){
            if (hasSeeds()){
                getSeeds();
                if (((JobFarmer)job).isTilled()){
                    state = State.HARVESTING;
                }else {
                    state = State.PLANTING;
                }
            }
        }
        if (state == State.PLANTING){
            

            }
        }else{
            delay--;
        }
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

    private void getSeeds(){
        int needed = farmerTile.getWidth() * farmerTile.getDepth();
        ItemStack seed = ((CropsBlock)CropsBlock.getBlockFromItem(farmerTile.getSeed().getItem())).getItem(world,targetPos,world.getBlockState(targetPos));
        for (BlockPos pos: chests){
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            if (chest != null){
                for (int i = 0; i< chest.getSizeInventory(); i++){
                    ItemStack stack = chest.getStackInSlot(i);
                    if (stack.getItem() == seed.getItem()){
                        int count = stack.getCount();
                        if (count <= needed){
                            needed -= count;
                            sim.getInventory().add(-1,stack);
                        }
                        if (needed <= 0){
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean hasSeeds(){
        int amount = 0;
        ItemStack seed = ((CropsBlock)CropsBlock.getBlockFromItem(farmerTile.getSeed().getItem())).getItem(world,targetPos,world.getBlockState(targetPos));
        for (BlockPos pos: chests){
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            if (chest != null){
                for (int i = 0; i< chest.getSizeInventory(); i++) {
                    ItemStack stack = chest.getStackInSlot(i);

                    if (stack.getItem() == seed.getItem()){
                        amount += stack.getCount();
                    }
                }
            }
        }
        return amount > 0;
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
        PLANTING,
        HARVESTING,
        RETURNING,


    }
}
