package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class MinerGoal extends MoveToBlockGoal {
    private SimEntity sim;
    private int tick;
    private IJob job;
    private BlockPos markerPos;
    private BlockPos offset;
    private int width; // how long it is to the left of the block
    private int depth; // how long it is to front of the block
    private int height; // how high it starts from above bedrock
    private int stair_num = 1;
    private Array stair_pos;
    private int side = 0;
    private Direction dir;
    private int delay = 20;

    private int progress;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int layer = 0; // used as what current depth the
    public MinerGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
        job = sim.getJob();
        this.sim = sim;

    }

    @Override
    public boolean shouldExecute() {
        super.shouldExecute();
        job = sim.getJob();
        if (job == null) return false;
        if (job.getWorkSpace() == null) return false;
        if (sim.world.getTileEntity(job.getWorkSpace()) == null )return false;
        if (((TileMiner) sim.world.getTileEntity(job.getWorkSpace())).getMarker() == null) return false;
        if (!((TileMiner) sim.world.getTileEntity(job.getWorkSpace())).CheckValidity()) return false;
        if (job.getWorkSpace() == null) return false;
        if (sim.world.getTileEntity(job.getWorkSpace()) == null) return false;
        if (true) return true; //Temporary
        if (job.getState() == EnumJobState.GOING_TO_WORK) {
            if (sim.func_233580_cy_().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                job.setState(EnumJobState.WORKING);
                return true;
            }else {

                sim.getNavigator().tryMoveToXYZ(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ(),sim.getAIMoveSpeed());
            }
        }


        return false;
    }


    @Override
    public void startExecuting() {
        super.startExecuting();
        TileMiner miner = ((TileMiner) sim.world.getTileEntity(job.getWorkSpace()));
        progress = ((JobMiner) sim.getJob()).getProgress();
        markerPos = miner.getMarker();
        dir = miner.getDir();
        offset = BlockPos.ZERO.offset(dir).offset(dir.rotateY()).add(0,-1,0);
        width = miner.getWidth() - 1;
        depth = miner.getDepth() - 1;
        height = miner.getYpos() - 1; // height from bedrock / y = 0
        stair_num = progress % (width*depth);
        stair_pos = findCurrentStairProgress();

    }

    @Override
    public void tick() {
        super.tick();
        if (delay <= 0) {

            progress = ((JobMiner) sim.getJob()).getProgress();

            ItemStack tool = sim.getHeldItemMainhand();
            if (!tool.equals(new ItemStack(Items.DIAMOND_PICKAXE))) {
                tool = new ItemStack(Items.DIAMOND_PICKAXE);
                sim.setHeldItem(Hand.MAIN_HAND, tool);
            }

            BlockPos minepos = offset;
            minepos = minepos.add(markerPos.getX(),markerPos.getY(),markerPos.getZ());


            if (column >= width){
                column = 0;
                row++;
            }
            if (row >= depth){
                row = 0;
                layer++;
            }

            minepos = minepos.offset(dir,row);
            minepos = minepos.offset(dir.rotateY(),column);
            minepos = minepos.offset(Direction.DOWN,layer);

            World world = sim.getEntityWorld();

           shouldMoveTo(sim.world,minepos);
            if (!shouldMoveTo(sim.world,minepos)){
                sim.getLookController().setLookPosition(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ()));
            Block block = sim.getEntityWorld().getBlockState(minepos).getBlock();
            if (block == Blocks.AIR){
                sim.world.setBlockState(minepos,Blocks.COBBLESTONE.getDefaultState(),2);
            }else {
                sim.world.setBlockState(minepos,Blocks.AIR.getDefaultState(),2);
            }
            if (block == Blocks.BEDROCK || minepos.getY() <= 0){
                sim.getJob().setState(EnumJobState.FORCE_STOP);
                return;
            }
            placeStairs(minepos); // runs placing stairs code
            //added mined block to inventory
            LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getEntityWorld())
                    .withRandom(world.rand)
                    .withParameter(LootParameters.POSITION, minepos)
                    .withParameter(LootParameters.TOOL, tool)
                    .withNullableParameter(LootParameters.BLOCK_ENTITY, world.getTileEntity(minepos));
            List<ItemStack> drops = block.getDefaultState().getDrops(builder);

            for (ItemStack stack : drops) {
                sim.getInventory().addItemStackToInventory(stack);
                }

                ((JobMiner) sim.getJob()).addProgress();
                column++;

            }

        delay = 5;

        }else{
            delay--;
        }
        tick++;
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos minepos) {
        this.destinationBlock = minepos;
        //sim.getNavigator().tryMoveToXYZ(minepos.getX(),minepos.getY()+1,minepos.getZ(),sim.getAIMoveSpeed());
        return !(sim.getPositionVec().distanceTo(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ())) < getTargetDistanceSq());
    }


    @Override
    public boolean shouldContinueExecuting() {
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == EnumJobState.FORCE_STOP) {
                ((JobMiner) job).setProgress(progress);
                sim.getJob().finishedWorkPeriod();
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
        return shouldExecute();
    }

    @Override
    public double getTargetDistanceSq() {
        return 5.0d;
    }


    private void placeStairs(BlockPos pos){
        Array current_stair_pos = new Array(column,row);
        System.out.println("stair num = " + stair_num);
        SimuKraft.LOGGER().debug("stair_num == layer " + (stair_num == layer));
        SimuKraft.LOGGER().debug("stair_pos == current_stair_pos " + (stair_pos.compare(current_stair_pos)));
        if (stair_num == layer && stair_pos.compare(current_stair_pos)){
            sim.world.setBlockState(pos,Blocks.OAK_STAIRS.getDefaultState());
            stair_num++;


        switch (side) {

            case 0:
                if (stair_pos.x >= width-1) {
                    side++;
                } else {
                    stair_pos.x++;
                    break;
                }

            case 1:
                if (stair_pos.y >= depth-1) {
                    side++;
                } else {
                    stair_pos.y++;
                    break;
                }

            case 2:
                if (stair_pos.x <= 0) {
                    side++;
                } else {
                    stair_pos.x--;
                    break;
                }

            case 3:
                if (stair_pos.y <= 0) {
                    side = 0;
                    stair_pos.x++;
                } else {
                    stair_pos.y--;
                }
                break;

            }
        }










    }


    private Array findCurrentStairProgress(){
        Array array = new Array();
        BlockPos pos = markerPos.add(offset);

        int x = 0;
        int y = 0;
        int z = 0;
        int side = 0;
        for (int i = 0; i < height; i++){
            BlockPos minepos = pos;
            minepos = minepos.offset(dir, y);
            minepos = minepos.offset(dir.rotateY(), x);
            minepos = minepos.offset(Direction.DOWN, z);
            sim.world.setBlockState(minepos.add(0,1,0),Blocks.COBBLESTONE.getDefaultState());
            if (sim.world.getBlockState(minepos).getBlock() != Blocks.OAK_STAIRS){
                break;
            }
            switch (side) {

                case 0:
                    if (x >= width-1) {
                        side++;
                    } else {
                        x++;
                        break;
                    }

                case 1:
                    if (y >= depth-1) {
                        side++;
                    } else {
                        y++;
                        break;
                    }

                case 2:
                    if (x <= 0) {
                        side++;
                    } else {
                        x--;
                        break;
                    }

                case 3:
                    if (y <= 0) {
                        side = 0;
                    } else {
                        y--;
                    }
                    break;

            }

        z++;
        }

        array.x = x;
        array.y = y;
        return array;
    }






    private static class Array {
        int x;
        int y;

        Array(){}

        Array(int x, int y){
            this.x = x;
            this.y = y;
        }

        boolean compare(Array array){
            return this.x == array.x && this.y == array.y;

        }
    }
}


