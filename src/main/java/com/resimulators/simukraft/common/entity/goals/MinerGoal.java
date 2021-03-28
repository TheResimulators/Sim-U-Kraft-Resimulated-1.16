package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class MinerGoal extends BaseGoal<JobMiner> {
    private final SimEntity sim;
    private int tick;
    private BlockPos markerPos;
    private BlockPos offset;
    private BlockPos minepos;
    private int width; // how long it is to the left of the block
    private int depth; // how long it is to front of the block
    private int height; // how high it starts from above bedrock
    private int stair_num = 1;
    private Array stair_pos;
    private int side = 0;
    private Direction dir;
    private int delay = 20;
    private static final DirectionProperty FACING = HorizontalBlock.FACING;
    private final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    private int progress;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int layer = 0; // used as what current depth the
    private Task currentTask = Task.NONE;
    public MinerGoal(SimEntity sim) {
        super(sim,sim.getSpeed()*2,20);
        this.sim = sim;

    }

    @Override
    public boolean canUse() {
        job = (JobMiner) sim.getJob();
        if (job == null) return false;
        if (job.getWorkSpace() == null) return false;
        if (sim.level.getBlockEntity(job.getWorkSpace()) == null) return false;
        if (((TileMiner) sim.level.getBlockEntity(job.getWorkSpace())).getMarker() == null) return false;
        if (!((TileMiner) sim.level.getBlockEntity(job.getWorkSpace())).CheckValidity()) return false;
        if (job.getWorkSpace() == null) return false;
        if (sim.level.getBlockEntity(job.getWorkSpace()) == null) return false;
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (sim.blockPosition().closerThan(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                sim.setActivity(Activity.WORKING);
                currentTask = Task.TRAVELING;
                return true;
            }
        }


        return false;
    }


    @Override
    public void start() {
        this.sim.getNavigation().stop();
        this.moveMobToBlock();
        this.tryTicks = 0;
        TileMiner miner = ((TileMiner) sim.level.getBlockEntity(job.getWorkSpace()));
        progress = ((JobMiner) sim.getJob()).getProgress();
        markerPos = miner.getMarker();
        dir = miner.getDir();
        offset = BlockPos.ZERO.relative(dir).relative(dir.getClockWise()).offset(0,-1,0);
        width = miner.getWidth() - 1;
        depth = miner.getDepth() - 1;
        height = miner.getYpos() - 1; // height from bedrock / y = 0
        stair_num = progress % (width*depth);
        stair_pos = findCurrentStairProgress();
        row = progress/width;
        column = progress%width;
        layer= progress%(width*depth);
        if (!sim.getInventory().hasItemStack(new ItemStack(Items.DIAMOND_PICKAXE)))
            sim.addItemStackToInventory(new ItemStack(Items.DIAMOND_PICKAXE));
    }

    @Override
    public void tick() {
        super.tick();
        World world = sim.getCommandSenderWorld();
        if (sim.getActivity() == Activity.WORKING) { // checks if the miner should be working

            if (delay <= 0 && currentTask == Task.MINING) {// checks if the miner should be mining or doing a different task

                progress = ((JobMiner) sim.getJob()).getProgress();

                ItemStack tool = sim.getMainHandItem();

                //get distance from current block targeted to be mined.
                if (sim.position().distanceTo(new Vector3d(minepos.getX(), minepos.getY(), minepos.getZ())) < 6) {
                    BlockState state = sim.getCommandSenderWorld().getBlockState(minepos);
                    Block block = state.getBlock();
                    if (block == Blocks.BEDROCK || minepos.getY() <= 1) {
                        currentTask = Task.RETURNING;
                    } else {
                        currentTask = Task.TRAVELING;
                    }

                    if (!block.isAir(state, world, minepos)) {
                        this.sim.getLookControl().setLookAt(new Vector3d(minepos.getX(), minepos.getY(), minepos.getZ()));
                        world.levelEvent(2001, minepos, Block.getId(state));
//                        Block.spawnDrops(world.getBlockState(minepos), world, minepos);
                        world.setBlock(minepos, Blocks.AIR.defaultBlockState(), 3);
                        this.sim.swing(Hand.MAIN_HAND);
                    }

                    placeStairs(minepos); // runs placing stairs code

                    //added mined block to inventory
                    LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld())
                            .withRandom(world.random)
                            .withParameter(LootParameters.TOOL, tool)
                            .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(minepos))
                            .withOptionalParameter(LootParameters.ORIGIN,Vector3d.atCenterOf(minepos));
                    List<ItemStack> drops = block.defaultBlockState().getDrops(builder);

                    for (ItemStack stack : drops) {
                        sim.getInventory().addItemStackToInventory(stack);
                    }
                    ((JobMiner) sim.getJob()).addProgress();
                    column++;

                }

                delay = 4;

            } else {
                delay--;
                if (delay < 0) {
                    delay = 0;
                }


                    }
            // traveling is used when is to far away from a block and needs to move closer to it
            if (currentTask == Task.TRAVELING) {
                minepos = offset;
                minepos = minepos.offset(markerPos.getX(), markerPos.getY(), markerPos.getZ());


                if (column >= width) {
                    column = 0;
                    row++;
                }
                if (row >= depth) {
                    row = 0;
                    layer++;
                }

                minepos = minepos.relative(dir, row);
                minepos = minepos.relative(dir.getClockWise(), column);
                minepos = minepos.relative(Direction.DOWN, layer);
                blockPos = minepos;
                if (minepos.closerThan(sim.position(),5)){
                    currentTask = Task.MINING;
                }else {
                    blockPos = minepos;
                }
            }

            tick++;
            // returning to base to empty inventory
            if (currentTask == Task.RETURNING) {
                BlockPos pos = sim.getJob().getWorkSpace();
                if ((sim.distanceToSqr(pos.getX(),pos.getY(),pos.getZ()) <= 5)) {
                    if (getInventoryAroundPos(sim.getJob().getWorkSpace()) != null) {
                        if (addSimInventoryToChest(getInventoryAroundPos(sim.getJob().getWorkSpace()))) {
                            sim.getJob().setState(Activity.NOT_WORKING);
                            currentTask = Task.NONE;
                            int id = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).getId();
                            sim.fireSim(sim, id, false);
                        } else {
                            SimuKraft.LOGGER().debug("Unable to move Items into chest");
                            SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(String.format("Sim %s in unable to empty its invetory into a chest at %s", sim.getDisplayName().getString(), sim.getJob().getWorkSpace().toString()), sim.level);
                        }
                    }
                }else{
                    blockPos = sim.getJob().getWorkSpace();
                }
            }
            if (currentTask == Task.NONE) {
                currentTask = Task.TRAVELING;
            }
        }
        Faction faction = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID());
        PlayerEntity player = world.getPlayerByUUID(faction.getOnlineFactionPlayer());
        // debugging \/
        if (player != null){
            player.displayClientMessage(new StringTextComponent("Working at: " + minepos + "; and navigator set at: " + sim.getNavigation().getTargetPos() + "; inventory state: " + (sim.getInventory().getFirstEmptyStack() == -1 ? "Full" : "Not Full")), true);
        }
    }

    @Override
    protected boolean isValidTarget(IWorldReader worldIn, BlockPos minepos) {
        return !(sim.position().distanceTo(new Vector3d(minepos.getX(),minepos.getY(),minepos.getZ())) < acceptedDistance());
    }

    @Override
    public boolean shouldRecalculatePath() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        //checks for different situations where the sim should stop working
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == Activity.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            }
        }
        (job).setProgress(progress);
        return canUse() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        // resets progress and a few other things to make things ready for the next time the sim works
        (job).setProgress(progress);
        sim.getJob().finishedWorkPeriod();
        sim.getJob().setState(Activity.NOT_WORKING);
    }

    @Override
    public double acceptedDistance() {
        return 3.0d;
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
                    placeBlock(current_stair_pos,pos.relative(dir.getCounterClockWise()),Blocks.OAK_STAIRS);
                } else{
                    placeBlock(current_stair_pos,pos,Blocks.OAK_STAIRS);
                }
                break;
            case 2:
                if (stair_pos.x <= 0) {// far left corner
                    placeBlock(current_stair_pos,pos,Blocks.OAK_SLAB);
                    current_stair_pos.y -= 1;
                    placeBlock(current_stair_pos,pos.relative(dir.getOpposite()),Blocks.OAK_STAIRS);
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
                sim.level.setBlockAndUpdate(pos,block.defaultBlockState().setValue(FACING,rotation));
                stair_num++;// increases the stair num

            }
            else if (block == Blocks.OAK_SLAB){ // the block to be placed is a slab
                sim.level.setBlockAndUpdate(pos,block.defaultBlockState().setValue(TYPE,SlabType.TOP));
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
        BlockPos pos = markerPos.offset(offset);
        int x = 0;
        int y = 0;
        int z = 0;
        int side =  0;
        for (int i = 0; i < height; i++){
            BlockPos minepos = pos;
            minepos = minepos.relative(dir, y);
            minepos = minepos.relative(dir.getClockWise(), x);
            minepos = minepos.relative(Direction.DOWN, z);
            if (sim.level.getBlockState(minepos).getBlock() != Blocks.OAK_STAIRS){
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
        dir.getClockWise();
        switch (side){

            case 0:
                return dir.getCounterClockWise();
            case 1:
                dir = dir.getOpposite();
                return dir;
            case 2:
                dir = dir.getClockWise();
                return dir;
            case 3:
                return dir;
        }

        return dir;
    }


    private ChestTileEntity getInventoryAroundPos(BlockPos pos){
        int range = 6;
        pos = pos.offset(-range/2,0,-range/2);
        BlockPos current = pos;
        for (int i = 0; i<range;i++){
            for (int j = 0; j < range; j++){
                current = pos.offset(i,0,j);
                TileEntity entity = sim.level.getBlockEntity(current);
                if (entity instanceof ChestTileEntity){
                    return (ChestTileEntity) entity;
                }


            }


        }
        return null;
    }

    private  boolean addSimInventoryToChest(ChestTileEntity chest){
        for (int i = 0;i<sim.getInventory().mainInventory.size();i++){
                ItemStack stack = sim.getInventory().mainInventory.get(i);
                if (!stack.equals(ItemStack.EMPTY) && !(stack.getItem() instanceof ToolItem)){
                    int index = findNextAvaliableSlot(chest);
                    if (index >= 0){
                    chest.setItem(index, stack);
                    sim.getInventory().removeItemNoUpdate(i);
                }else {
                    SimuKraft.LOGGER().debug("No Room in chest");
                    return false;
                }
            }
        }
        return true;
    }

    private int findNextAvaliableSlot(ChestTileEntity chest){
        for (int i = 0;i< chest.getContainerSize();i++){
            ItemStack stack = chest.getItem(i);
            if (stack == ItemStack.EMPTY){
                return i;
            }

        }

        return -1;
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

    enum Task{
        MINING,
        RETURNING,
        WAITING,
        TRAVELING,
        NONE

    }
}


