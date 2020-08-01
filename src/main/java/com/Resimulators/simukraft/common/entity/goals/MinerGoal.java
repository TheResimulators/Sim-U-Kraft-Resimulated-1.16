package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.state.DirectionProperty;
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
    private static DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    private EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
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
        shouldMoveTo(sim.world,minepos);
        if (delay <= 0) {

            progress = ((JobMiner) sim.getJob()).getProgress();

            ItemStack tool = sim.getHeldItemMainhand();
            if (!tool.equals(new ItemStack(Items.DIAMOND_PICKAXE))) {
                tool = new ItemStack(Items.DIAMOND_PICKAXE);
                sim.setHeldItem(Hand.MAIN_HAND, tool);
            }


            World world = sim.getEntityWorld();


            if (sim.getPositionVec().distanceTo(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ())) < 5){
                sim.getLookController().setLookPosition(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ()));
                Block block = sim.getEntityWorld().getBlockState(minepos).getBlock();
                sim.world.setBlockState(minepos,Blocks.AIR.getDefaultState(),2);
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
                destinationBlock = BlockPos.ZERO;
                }

        delay = 2;

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

    /** Used to check where the next stair needs to be placed and places it. does not handle rotation
     * handles placing slabs and the corners.
     * */
    private void placeStairs(BlockPos pos){

        Array current_stair_pos = new Array(column,row);//creates new array with the current mining position column, row

        switch (side) { // checks whether the block placed should be a slab or a stair
            case 0:
                if (stair_pos.x >= width-1) {// first corner to the right of the block
                    placeBlock(current_stair_pos,pos,Blocks.OAK_SLAB);
                }else{
                    placeBlock(current_stair_pos,pos,Blocks.OAK_STAIRS);
                }
                break;
            case 1:
                if (stair_pos.y >= depth-1) { // for opposite corner
                    placeBlock(current_stair_pos,pos,Blocks.OAK_SLAB);
                    current_stair_pos.x -= 1;
                    placeBlock(current_stair_pos,pos.offset(dir.rotateYCCW()),Blocks.OAK_STAIRS);
                } else{
                    placeBlock(current_stair_pos,pos,Blocks.OAK_STAIRS);
                }
                break;
            case 2:
                if (stair_pos.x <= 0) {// far left corner
                    placeBlock(current_stair_pos,pos,Blocks.OAK_SLAB);
                    current_stair_pos.y -= 1;
                    placeBlock(current_stair_pos,pos.offset(dir.getOpposite()),Blocks.OAK_STAIRS);
                }else{
                    placeBlock(current_stair_pos,pos,Blocks.OAK_STAIRS);
                }
                break;
            case 3:
                if (stair_pos.y <= 0) { // first block but not the first block placed
                    placeBlock(current_stair_pos,pos,Blocks.OAK_SLAB);
                }else {
                    placeBlock(current_stair_pos,pos,Blocks.OAK_STAIRS);
                }
                break;


        }


        }

    private void placeBlock(Array current_pos,BlockPos pos,Block block){
        if (stair_num == layer && stair_pos.compare(current_pos)){
            Direction rotation = getStairRotation(side); // gets the rotation the stair should be in
            if (block == Blocks.OAK_STAIRS){ // checks if it should be a stair to be placed
                sim.world.setBlockState(pos,block.getDefaultState().with(FACING,rotation));
                stair_num++;// increases the stair num

            }
            else if (block == Blocks.OAK_SLAB){ // the block to be placed is a slab
                sim.world.setBlockState(pos,block.getDefaultState().with(TYPE,SlabType.TOP));
            }
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
            }}

    }

    private Array findCurrentStairProgress(){
        Array array = new Array();
        BlockPos pos = markerPos.add(offset);
        int x = 0;
        int y = 0;
        int z = 0;
        int side =  0;
        for (int i = 0; i < height; i++){
            BlockPos minepos = pos;
            minepos = minepos.offset(dir, y);
            minepos = minepos.offset(dir.rotateY(), x);
            minepos = minepos.offset(Direction.DOWN, z);
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


    private Direction getStairRotation(int side){
        Direction dir = this.dir;
        dir.rotateY();
        switch (side){

            case 0:
                return dir.rotateYCCW();
            case 1:
                dir = dir.getOpposite();
                return dir;
            case 2:
                dir = dir.rotateY();
                return dir;
            case 3:
                return dir;
        }

        return dir;
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


