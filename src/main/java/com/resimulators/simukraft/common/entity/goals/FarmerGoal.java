package com.resimulators.simukraft.common.entity.goals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobFarmer;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;

public class FarmerGoal extends MoveToBlockGoal {
    private SimEntity sim;
    private final World world;
    private int tick;
    private JobFarmer job;
    private int delay = 20;
    private BlockPos targetPos;
    private BlockPos offset;
    private Direction dir;
    private State state;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private TileMarker marker;
    private TileFarmer farmerTile;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int width;
    private int depth;
    private int progress;

    private static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState(), Blocks.DIRT, Blocks.FARMLAND.getDefaultState()));
    public FarmerGoal(SimEntity sim) {
        super(sim, 2*sim.getAIMoveSpeed(), 20);
        this.sim = sim;
        this.world = sim.getEntityWorld();
    }
    @Override
    public boolean shouldExecute() {
        job = (JobFarmer) sim.getJob();
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
        dir = farmerTile.getDir();
        farmerTile = (TileFarmer) world.getTileEntity(job.getWorkSpace());
        offset = BlockPos.ZERO.offset(dir).offset(dir.rotateY()).add(0,-1,0);
        findChests();
        progress = job.getProgress();
        width = farmerTile.getWidth();
        depth = farmerTile.getDepth();
        row = progress/width;
        column = progress%width;

    }

    @Override
    public void tick() {
        super.tick();
        if (delay <= 0) {
            if (state == State.RESOURCES) {
                if (hasSeeds()) {
                    getSeeds();
                    if (job.isTilled()) {
                        state = State.HARVESTING;
                    } else {
                        state = State.PLANTING;
                    }
                }
            }
            if (state == State.PLANTING) {
                if (targetPos.withinDistance(sim.getPositionVec(), 5)) {
                    BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(targetPos).getBlock());
                    if (blockstate != null) {
                        world.playSound(null, targetPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        if (!world.isRemote) {
                            world.setBlockState(targetPos, blockstate, 11);
                        }
                    }
                }

            if (findNextTarget()) {
                job.setProgress(progress++);
                targetPos = farmerTile.getPos().add(offset).offset(dir, row);
                targetPos = targetPos.offset(dir.rotateY(), column);
                destinationBlock = targetPos;
            } else {
                state = State.HARVESTING;
                }
            }
        } else {
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


    private boolean findNextTarget(){
        if (row > depth){
            column = 0;
            row = 0;
            return false;
        }
        else if (column > width){
            column = 0;
            row++;
            return true;
        }else{
            column++;
            return true;
        }
    }




    private enum State{
        RESOURCES,
        PLANTING,
        HARVESTING,
        RETURNING,


    }
}
