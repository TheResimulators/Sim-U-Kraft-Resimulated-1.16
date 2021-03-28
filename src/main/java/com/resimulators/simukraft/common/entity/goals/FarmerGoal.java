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


    private static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT, Blocks.FARMLAND.defaultBlockState()));

    public FarmerGoal(SimEntity sim) {
        super(sim, 2 * sim.getSpeed(), 20);
        this.sim = sim;
        this.world = sim.getCommandSenderWorld();

    }

    @Override
    public boolean canUse() {
        job = (JobFarmer) sim.getJob();

        if (job != null) {
            chests = findChestAroundTargetBlock(job.getWorkSpace(), 5, world);
            if (sim.getActivity() == Activity.GOING_TO_WORK) {
                if (sim.blockPosition().closerThan(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                    farmerTile = (TileFarmer) world.getBlockEntity(job.getWorkSpace());
                    if (farmerTile != null) {
                        dir = farmerTile.getDir();
                    }
                    if (hasSeeds()) {
                        sim.setActivity(Activity.WORKING);
                        return true;
                    } else {
                        SavedWorldData.get(world).getFactionWithSim(sim.getUUID()).sendFactionChatMessage((sim.getDisplayName().getString() + " does not have the seeds required to work" + "(" + farmerTile.getSeed().getName() + ")"), world);
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void start() {
        sim.setItemInHand(sim.getUsedItemHand(), Items.DIAMOND_HOE.getDefaultInstance());

        state = State.RESOURCES;

        offset = BlockPos.ZERO.relative(dir).relative(dir.getClockWise()).offset(0, -1, 0);
        chests = findChestAroundTargetBlock(job.getWorkSpace(), 5, world);
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
            SavedWorldData.get(world).getFactionWithSim(sim.getUUID()).subCredits(0.2);
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
                    if (targetPos.closerThan(sim.position(), 5)) {

                        if ((row % 4 == 0 && column % 4 == 0) && (row > 0 || column > 0)) {
                            world.setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                            if (findNextTarget()) {
                                job.setProgress(progress++);
                                setDestination();
                            }
                        } else {
                            BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(targetPos).getBlock());
                            if (blockstate != null) {
                                world.playSound(null, targetPos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                if (!world.isClientSide) {
                                    world.setBlock(targetPos, blockstate, 11);
                                    world.setBlockAndUpdate(targetPos.above(), farmerTile.getSeed().getBlock().defaultBlockState());
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
            if (targetPos.closerThan(sim.position(), 3)) {

                LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld())
                        .withRandom(world.random)
                        .withParameter(LootParameters.TOOL, sim.getUseItem())
                        .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(targetPos))
                        .withParameter(LootParameters.ORIGIN,Vector3d.atBottomCenterOf(targetPos));
                List<ItemStack> drops = world.getBlockState(targetPos).getDrops(builder);

                for (ItemStack stack : drops) {
                    stack.addTagElement("harvested",new CompoundNBT());
                    sim.getInventory().addItemStackToInventory(stack);
                }
                world.setBlockAndUpdate(targetPos, farmerTile.getSeed().getBlock().defaultBlockState());
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
            if (job.getWorkSpace().closerThan(sim.position(),5)){
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
    public double acceptedDistance() {
        return 2.0d;
    }

    @Override
    public boolean canContinueToUse() {
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == Activity.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            }
        }

        return canUse() && super.canContinueToUse();
    }

    private void getSeeds() {
        int needed = farmerTile.getWidth() * farmerTile.getDepth();
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getCloneItemStack(world, job.getWorkSpace(), world.getBlockState(job.getWorkSpace()));
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
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
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getCloneItemStack(world, job.getWorkSpace(), world.getBlockState(job.getWorkSpace()));
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);

                    if (stack.getItem() == seed.getItem()) {
                        amount += stack.getCount();
                    }
                }
            }
        }
        return amount > 0;
    }


    @Override
    protected boolean isValidTarget(IWorldReader worldIn, BlockPos pos) {
        return sim.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < acceptedDistance();
    }


    private void setDestination() {
        targetPos = farmerTile.getBlockPos().below().relative(dir, row);
        targetPos = targetPos.relative(dir).relative(dir.getClockWise(), column);
        blockPos = targetPos;
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
            for (int i = 0; i < inventory.getContainerSize();i++){
                ItemStack stack = inventory.getItem(i);
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
        ItemStack stack = sim.getInventory().getItem(itemsIndex.get(0));

        if (stack != null){
            if (stack.getTag() != null){
                stack.getTag().remove("harvested");
            }
            sim.getInventory().removeItemNoUpdate(itemsIndex.get(0));
            for (BlockPos pos: chests){
                ChestTileEntity tileEntity = (ChestTileEntity) world.getBlockEntity(pos);
                if (tileEntity != null){
                    for (int i = 0; i < tileEntity.getContainerSize();i++){
                        int count = stack.getCount();
                        if (tileEntity.getItem(i).getItem() == stack.getItem()){
                            ItemStack chestStack = tileEntity.getItem(i);
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
                        } else if (tileEntity.getItem(i).getItem() == Items.AIR) {
                                tileEntity.setItem(i, stack);
                                return true;

                        }
                    }
                }
            }
        }
        
        return false;
    }


    private boolean checkForHarvestable() {
        ArrayList<BlockPos> harvestable = BlockPos.betweenClosedStream(job.getWorkSpace().relative(dir), job.getWorkSpace().relative(dir, depth).relative(dir.getClockWise(), width))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof CropsBlock)
                .filter(blockPos -> ((CropsBlock)world.getBlockState(blockPos).getBlock()).isMaxAge(world.getBlockState(blockPos)))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(blockPos -> sim.position().distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));
        return harvestable.size() > 0;
    }

    private boolean getNextHarvestable() {
        BlockPos pos = null;
        ArrayList<BlockPos> harvestable = BlockPos.betweenClosedStream(job.getWorkSpace().relative(dir), job.getWorkSpace().relative(dir, depth+1).relative(dir.getClockWise(), width+1))
                .filter(blockPos -> {
                    if (world.getBlockState(blockPos).getBlock() instanceof CropsBlock) {
                        return ((CropsBlock) world.getBlockState(blockPos).getBlock()).isMaxAge(world.getBlockState(blockPos));
                    }
                    return false;
                })
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(blockPos -> sim.position().distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));

        if (harvestable.size() > 0) {
            pos = harvestable.get(0);
            targetPos = pos;
            blockPos = pos;
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

