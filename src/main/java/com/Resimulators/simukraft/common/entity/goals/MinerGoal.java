package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.List;

public class MinerGoal extends MoveToBlockGoal {
    private SimEntity sim;
    private int tick;
    private IJob job;
    private BlockPos markerPos;
    private BlockPos offset;
    private int width;
    private int depth;
    private int height;
    private Direction dir;
    private int delay = 20;

    private int progress;
    private int column = 0;
    private int row = 0;
    private int layer = 0;
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
        if (true) return true;
        if (job.getState() == EnumJobState.GOING_TO_WORK) {
            if (sim.func_233580_cy_().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                job.setState(EnumJobState.WORKING);
                return true;
            }else {
               // sim.getMoveHelper().setMoveTo(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ(),sim.getAIMoveSpeed());
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
        height = miner.getYpos()-1; // height from bedrock / y = 0


        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                sim.world.setBlockState(markerPos.add(offset.getX(), offset.getY() + 4, offset.getZ()).offset(dir, z).offset(dir.rotateY(), x), Blocks.COBBLESTONE.getDefaultState());
                BlockPos pos = markerPos.add(offset.getX(), offset.getY() + 4, offset.getZ()).offset(dir, z).offset(dir.rotateY(), x);
                //System.out.println(pos);

            }
        }

        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println("blockadde");
        System.out.println((width+1)*(depth+1)*(height+1));
        /**for (int i = 0; i < (width+1)*(depth+1)*(height+1);i++){
            BlockPos minepos = markerPos.add(offset.getX(), offset.getY(),offset.getZ());
            System.out.println("column: " +column+"  row: "+row+" layer: "+ layer);
            if (column >= width+1){
                column = 0;
                row++;
            }
            if (row >= depth+1){
                row = 0;
                layer++;
            }
            if (layer >= height+1){
                continue;
            }

            minepos = minepos.offset(dir,row);
            minepos = minepos.offset(dir.rotateY(),column);
            minepos = minepos.offset(Direction.DOWN,layer);
           // sim.world.setBlockState(minepos, Blocks.AIR.getDefaultState());
           // sim.world.setBlockState(minepos.add(0,10,0), Blocks.DIRT.getDefaultState());
            column++;


        }**/
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
            //if (layer > height){
             //
            //}

            minepos = minepos.offset(dir,row);
            minepos = minepos.offset(dir.rotateY(),column);
            minepos = minepos.offset(Direction.DOWN,layer);

            World world = sim.getEntityWorld();
            SimuKraft.LOGGER().debug(sim.getNavigator().getPath());

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
            //sim.world.setBlockState(minepos.add(0,5,0),Blocks.COBBLESTONE.getDefaultState(),2);
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
                column++;}
           // sim.getNavigator().tryMoveToXYZ(minepos.getX(),minepos.getY()+1,minepos.getZ(),sim.getAIMoveSpeed());
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
        return !(sim.getPositionVec().distanceTo(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ())) < 5);
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

}


