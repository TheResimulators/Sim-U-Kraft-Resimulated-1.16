package com.resimulators.simukraft.common.entity.goals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.entity.sim.SimInventory;
import com.resimulators.simukraft.common.jobs.JobFarmer;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FarmerGoal extends BaseGoal<JobFarmer> {
    private final World world;
    private int tick;
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
        super(sim, 2 * sim.getAIMoveSpeed(), 20);
        this.sim = sim;
        this.world = sim.getEntityWorld();

    }

    @Override
    public boolean shouldExecute() {
        job = (JobFarmer) sim.getJob();

        if (job != null) {
            chests = new ArrayList<>();
            findChests();
            if (sim.getActivity() == Activity.GOING_TO_WORK) {
                if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                    farmerTile = (TileFarmer) world.getTileEntity(job.getWorkSpace());
                    if (farmerTile != null) {
                        dir = farmerTile.getDir();
                    }
                    if (hasSeeds()) {
                        sim.setActivity(Activity.WORKING);
                        return true;
                    } else {
                        SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage((sim.getDisplayName().getString() + " does not have the seeds required to work" + "(" + farmerTile.getSeed().getName() + ")"), world);
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(), Items.DIAMOND_HOE.getDefaultInstance());

        state = State.RESOURCES;

        offset = BlockPos.ZERO.offset(dir).offset(dir.rotateY()).add(0, -1, 0);
        findChests();
        progress = job.getProgress();
        width = farmerTile.getWidth()-1;
        depth = farmerTile.getDepth();
        row = progress / width;
        column = progress % width;
        setDestination();
        if (checkForHarvestable()){
            state = State.HARVESTING;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (delay <= 0) {
            SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID()).subCredits(0.2);
            delay = 5;
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
                if (targetPos != null) {
                    if (targetPos.withinDistance(sim.getPositionVec(), 5)) {

                        if ((row % 4 == 0 && column % 4 == 0) && (row > 0 || column > 0)) {
                            world.setBlockState(targetPos, Blocks.WATER.getDefaultState());
                            if (findNextTarget()) {
                                job.setProgress(progress++);
                                setDestination();
                            }
                        } else {
                            BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(targetPos).getBlock());
                            if (blockstate != null) {
                                world.playSound(null, targetPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                if (!world.isRemote) {
                                    world.setBlockState(targetPos, blockstate, 11);
                                    world.setBlockState(targetPos.up(), farmerTile.getSeed().getBlock().getDefaultState());
                                    if (findNextTarget()) {
                                        job.setProgress(progress++);
                                        setDestination();
                                    } else {
                                        if (checkForHarvestable()) {
                                            state = State.HARVESTING;
                                        } else {
                                            state = State.WAITING;

                                        }
                                    }
                                }
                            } else {
                                if (world.getBlockState(targetPos).getBlock() == Blocks.FARMLAND){
                                    if (findNextTarget()) {
                                        job.setProgress(progress++);
                                        setDestination();
                                    } else {
                                        if (checkForHarvestable()) {
                                            state = State.HARVESTING;
                                        } else {
                                            state = State.WAITING;

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state == State.HARVESTING) {
            if (world.getBlockState(targetPos).getBlock() instanceof CropsBlock){
            if (targetPos.withinDistance(sim.getPositionVec(), 3)) {

                LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getEntityWorld())
                        .withRandom(world.rand)
                        .withParameter(LootParameters.TOOL, sim.getActiveItemStack())
                        .withNullableParameter(LootParameters.BLOCK_ENTITY, world.getTileEntity(targetPos))
                        .withParameter(LootParameters.field_237457_g_,Vector3d.copyCenteredHorizontally(targetPos));
                List<ItemStack> drops = world.getBlockState(targetPos).getDrops(builder);

                for (ItemStack stack : drops) {
                    stack.setTagInfo("harvested",new CompoundNBT());
                    sim.getInventory().addItemStackToInventory(stack);
                }
                world.setBlockState(targetPos, farmerTile.getSeed().getBlock().getDefaultState());
                sim.getInventory().getItemStack();
                if (checkForHarvestable()){
                    getNextHarvestable();
                }else{
                    state = State.STORING;
                    sim.getJob().setState(Activity.NOT_WORKING);
                }
            }
        }else{
                if (checkForHarvestable()){
                    getNextHarvestable();
                }else{
                    state = State.STORING;
                }
            }
        }
        if (state == State.STORING){
            if (job.getWorkSpace().withinDistance(sim.getPositionVec(),5)){
                ArrayList<Integer> itemIndex = getHarvestItems();
                if (itemIndex.size() > 0){
                    if (!addItemToInventory(itemIndex)){
                        state = State.WAITING;
                    }
                }else{
                    state = State.WAITING;
                }

            }
        }
        if (state == State.WAITING) {
            if (checkForHarvestable()) {
                state = State.HARVESTING;
                getNextHarvestable();
            }else{
                sim.getJob().setState(Activity.FORCE_STOP);
            }
        if (row > depth){
            state = State.WAITING;
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
            if (sim.getJob().getState() == Activity.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            }
        }

        return shouldExecute() && super.shouldContinueExecuting();
    }

    private void getSeeds() {
        int needed = farmerTile.getWidth() * farmerTile.getDepth();
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getItem(world, job.getWorkSpace(), world.getBlockState(job.getWorkSpace()));
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getSizeInventory(); i++) {
                    ItemStack stack = chest.getStackInSlot(i);
                    if (stack.getItem() == seed.getItem()) {
                        int count = stack.getCount();
                        if (count <= needed) {
                            needed -= count;
                            sim.getInventory().add(-1, stack);
                        }
                        if (needed <= 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean hasSeeds() {
        int amount = 0;
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getItem(world, job.getWorkSpace(), world.getBlockState(job.getWorkSpace()));
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getSizeInventory(); i++) {
                    ItemStack stack = chest.getStackInSlot(i);

                    if (stack.getItem() == seed.getItem()) {
                        amount += stack.getCount();
                    }
                }
            }
        }
        return amount > 0;
    }

    private void findChests() {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundAndBelowPosition(job.getWorkSpace(), 5);
        for (BlockPos pos : blocks) {
            if (world.getTileEntity(pos) instanceof ChestTileEntity) {
                chests.add(pos);
            }
        }
    }


    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return sim.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < getTargetDistanceSq();
    }


    private void setDestination() {
        targetPos = farmerTile.getPos().down().offset(dir, row);
        targetPos = targetPos.offset(dir).offset(dir.rotateY(), column);
        destinationBlock = targetPos;
    }

    private boolean findNextTarget() {



        if (column > width) {
            column = 0;
            row++;
        } else {
            column++;
        }
        if (row > depth) {
            column = 0;
            row = 0;
            return false;
        }
        return true;
    }

    private ArrayList<Integer> getHarvestItems(){
        ArrayList<Integer> harvestItems = new ArrayList<>();
        SimInventory inventory = sim.getInventory();

        if (inventory != null){
            for (int i = 0; i < inventory.getSizeInventory();i++){
                ItemStack stack = inventory.getStackInSlot(i);
                CompoundNBT nbt = stack.getTag();
                if (nbt != null){
                if (stack.getTag().contains("harvested")){
                    harvestItems.add(i);
                    }
                }
            }

        }

        return harvestItems;
    }

    private boolean addItemToInventory(ArrayList<Integer> itemsIndex){
        ItemStack stack = sim.getInventory().getStackInSlot(itemsIndex.get(0));

        if (stack != null){
            if (stack.getTag() != null){
                stack.getTag().remove("harvested");
            }
            sim.getInventory().removeStackFromSlot(itemsIndex.get(0));
            for (BlockPos pos: chests){
                ChestTileEntity tileEntity = (ChestTileEntity) world.getTileEntity(pos);
                if (tileEntity != null){
                    for (int i = 0; i < tileEntity.getSizeInventory();i++){
                        int count = stack.getCount();
                        if (tileEntity.getStackInSlot(i).getItem() == stack.getItem()){
                            ItemStack chestStack = tileEntity.getStackInSlot(i);
                            if (chestStack.getCount() < chestStack.getMaxStackSize()){
                                if (chestStack.getCount() + count >= chestStack.getMaxStackSize()){
                                int difference = chestStack.getMaxStackSize() - chestStack.getCount();
                                chestStack.grow(difference);
                                count -= difference;
                                stack.shrink(difference);
                            } else{
                                chestStack.grow(count);
                                count = 0;
                                stack.shrink(stack.getCount());
                            }
                            if (count <= 0){
                                return true;
                                }
                            }
                        } else if (tileEntity.getStackInSlot(i).getItem() == Items.AIR) {
                                tileEntity.setInventorySlotContents(i, stack);
                                return true;

                        }
                    }
                }
            }
        }
        
        return false;
    }


    private boolean checkForHarvestable() {
        ArrayList<BlockPos> harvestable = BlockPos.getAllInBox(job.getWorkSpace().offset(dir), job.getWorkSpace().offset(dir, depth).offset(dir.rotateY(), width))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof CropsBlock)
                .filter(blockPos -> ((CropsBlock)world.getBlockState(blockPos).getBlock()).isMaxAge(world.getBlockState(blockPos)))
                .map(BlockPos::toImmutable)
                .sorted(Comparator.comparingDouble(blockPos -> sim.getPositionVec().squareDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));
        return harvestable.size() > 0;
    }

    private boolean getNextHarvestable() {
        BlockPos pos = null;
        ArrayList<BlockPos> harvestable = BlockPos.getAllInBox(job.getWorkSpace().offset(dir), job.getWorkSpace().offset(dir, depth+1).offset(dir.rotateY(), width+1))
                .filter(blockPos -> {
                    if (world.getBlockState(blockPos).getBlock() instanceof CropsBlock) {
                        return ((CropsBlock) world.getBlockState(blockPos).getBlock()).isMaxAge(world.getBlockState(blockPos));
                    }
                    return false;
                })
                .map(BlockPos::toImmutable)
                .sorted(Comparator.comparingDouble(blockPos -> sim.getPositionVec().squareDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));

        if (harvestable.size() > 0) {
            pos = harvestable.get(0);
            targetPos = pos;
            destinationBlock = pos;
        }
        return pos != null;
    }

    private enum State {
        RESOURCES,
        PLANTING,
        HARVESTING,
        STORING,
        WAITING


    }
}

