package com.resimulators.simukraft.common.jobs.reworked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.resimulators.simukraft.common.entity.goals.BaseGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.entity.sim.SimInventory;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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

public class JobFarmer implements IReworkedJob {
    private static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT, Blocks.FARMLAND.defaultBlockState()));
    private final World world;
    private final SimEntity sim;
    private int periodsworked = 0;
    private BlockPos workSpace, blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private State state;
    private boolean tilled = false;
    private int progress;
    private boolean finished;
    private int tick;
    private int delay = 20;
    private BlockPos targetPos;
    private BlockPos offset;
    private Direction dir;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private TileMarker marker;
    private TileFarmer farmerTile;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int width;
    private int depth;

    public JobFarmer(SimEntity sim) {
        this.sim = sim;
        this.world = sim.getCommandSenderWorld();
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Profession jobType() {
        return Profession.FARMER;
    }

    @Override
    public int intervalTime() {
        return 1000;
    }


    @Override
    public int workTime() {
        return 10000;  //negative one so that it can work as much as it can. builder should work all day.
                       //if it can't find resources it take a 1000 tick break
    }

    @Override
    public int maximumWorkPeriods() {
        return 10;

    }

    @Override
    public boolean nightShift() {
        return false;
    }

    @Override
    public int getPeriodsWorked() {
        return periodsworked;
    }

    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT data = new CompoundNBT();
        data.putInt("id", sim.getProfession());
        nbt.add(data);
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.asLong());
        }
        if (blockPos != null) {
            other.putLong("blockpos", blockPos.asLong());
        }
        if (dir != null) {
            other.putInt("dir", dir.ordinal());
        }
        other.putInt("state", state.ordinal());
        other.putBoolean("tilled", tilled);
        other.putBoolean("finished", finished);
        nbt.add(other);

        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsworked = list.getInt("periodsworked");
            }
            if (list.contains("jobpos")) {
                setWorkSpace(BlockPos.of(list.getLong("jobpos")));
            }
            if (list.contains("tilled")) {
                setTilled(list.getBoolean("tilled"));
            }
            if (list.contains("finished")) {
                finished = list.getBoolean("finished");
            }
            if (list.contains("blockpos")) {
                blockPos = BlockPos.of(list.getLong("blockpos"));
            }
            if (list.contains("state")) {
                state = State.values()[list.getInt("state")];
            }
            if (list.contains("dir")) {
                dir = Direction.values()[list.getInt("dir")];
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(++periodsworked);
    }

    @Override
    public void setWorkedPeriods(int periods) {
        periodsworked = periods;
    }

    @Override
    public void resetPeriodsWorked() {
        setWorkedPeriods(0);
    }

    @Override
    public BlockPos getWorkSpace() {
        return workSpace;
    }

    @Override
    public void setWorkSpace(BlockPos pos) {
        this.workSpace = pos;
    }

    @Override
    public double getWage() {
        return 0.2;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public void start() {
        farmerTile = (TileFarmer) world.getBlockEntity(getWorkSpace());
        if (farmerTile.getMarker() != null){
            sim.setItemInHand(sim.getUsedItemHand(), Items.DIAMOND_HOE.getDefaultInstance());
            state = State.RESOURCES;

            if (farmerTile != null) {
                dir = farmerTile.getDir();
            }
            chests = BaseGoal.findChestsAroundTargetBlock(getWorkSpace(), 5, world);
            width = farmerTile.getWidth() - 1;
            depth = farmerTile.getDepth() - 1;
            row = 0;
            column = 0;
            setDestination();
            if (checkForHarvestable()) {
                state = State.HARVESTING;
            }

            sim.setActivity(Activity.WORKING);
        }
    }

    private void setDestination() {
        targetPos = farmerTile.getMarker().below().relative(dir, row + 1).relative(dir.getClockWise(), column + 1);
        blockPos = targetPos;
    }

    private boolean checkForHarvestable() {
        ArrayList<BlockPos> harvestable = BlockPos.betweenClosedStream(farmerTile.getMarker().relative(dir, 1).relative(dir.getClockWise(), 1), farmerTile.getMarker().relative(dir, depth + 1).relative(dir.getClockWise(), width + 1))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof CropsBlock)
                .filter(blockPos -> ((CropsBlock) world.getBlockState(blockPos).getBlock()).isMaxAge(world.getBlockState(blockPos)))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(blockPos -> sim.position().distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));
        return harvestable.size() > 0;
    }

    public void tick() {
        if (farmerTile == null)sim.setActivity(Activity.FORCE_STOP);
        if (farmerTile.getMarker() == null) sim.setActivity(Activity.FORCE_STOP);
        if (blockPos != null && !sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 2))
            sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
        chests = Utils.findInventoriesAroundPos(getWorkSpace(), 5, world);
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (sim.blockPosition().closerThan(new Vector3d(getWorkSpace().getX(), getWorkSpace().getY(), getWorkSpace().getZ()), 5)) {
                if (hasSeeds()) {
                    sim.setActivity(Activity.WORKING);
                } else {
                    SavedWorldData.get(world).getFactionWithSim(sim.getUUID()).sendFactionChatMessage((sim.getDisplayName().getString() + " does not have the seeds required to work" + "(" + farmerTile.getSeed().getName() + ")"), world);
                    state = State.RESOURCES;
                }
            }
        }
        if (delay <= 0) {
            SavedWorldData.get(world).getFactionWithSim(sim.getUUID()).subCredits(0.2);
            delay = 5;
            if (state == State.RESOURCES) {
                if (hasSeeds()) {
                    getSeeds();
                    if (checkForHarvestable()) {
                        state = State.HARVESTING;
                    } else if (!isTilled()) {
                        seekUntilled();
                        state = State.PLANTING;
                        setDestination();
                    } else {
                        state = State.WAITING;
                    }
                } else {
                    //still no seeds
                }
            }
            if (state == State.PLANTING) {
                if (targetPos != null) {
                    if (targetPos.closerThan(sim.position(), 5)) {

                        if ((row % 4 == 0 && column % 4 == 0) && (row > 0 || column > 0)) {
                            world.setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                            if (findNextTarget()) {
                                progress++;
                                setDestination();
                            }
                        } else {
                            BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(targetPos).getBlock());
                            if (blockstate != null) {
                                world.playSound(null, targetPos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                if (!world.isClientSide) {
                                    world.setBlock(targetPos, blockstate, 11);
                                    world.setBlockAndUpdate(targetPos.above(), farmerTile.getSeed().getBlock().defaultBlockState());
                                    if (!isTilled()) {
                                        seekUntilled();
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
                                if (world.getBlockState(targetPos).getBlock() == Blocks.FARMLAND) {
                                    if (!isTilled()) {
                                        seekUntilled();
                                        setDestination();
                                    } else {
                                        if (checkForHarvestable()) {
                                            state = State.HARVESTING;
                                        } else {
                                            state = State.WAITING;

                                        }
                                    }
                                } else {
                                    state = State.WAITING;
                                }
                            }
                        }
                    } else {
                        blockPos = targetPos;
                        //sim.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), sim.getSpeed() * 2);
                    }
                } else {
                    setDestination();
                }
            }
        }

        if (state == State.HARVESTING) {
            //getNextHarvestable();
            if (world.getBlockState(targetPos).getBlock() instanceof CropsBlock) {
                if (targetPos.closerThan(sim.position(), 3)) {

                    LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld())
                            .withRandom(world.random)
                            .withParameter(LootParameters.TOOL, sim.getUseItem())
                            .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(targetPos))
                            .withParameter(LootParameters.ORIGIN, Vector3d.atBottomCenterOf(targetPos));
                    List<ItemStack> drops = world.getBlockState(targetPos).getDrops(builder);

                    for (ItemStack stack : drops) {
                        stack.addTagElement("harvested", new CompoundNBT());
                        sim.getInventory().addItemStackToInventory(stack);
                    }
                    world.setBlockAndUpdate(targetPos, farmerTile.getSeed().getBlock().defaultBlockState());
                    sim.getInventory().getItemStack();
                    if (checkForHarvestable()) {
                        getNextHarvestable();
                    } else {
                        state = State.STORING;
                        //sim.getJob().setState(Activity.NOT_WORKING);
                    }
                } else {
                    //blockPos = targetPos;
                }
            } else {
                if (checkForHarvestable()) {
                    getNextHarvestable();
                } else {
                    state = State.STORING;
                }
            }
        }
        if (state == State.STORING) {
            if (getWorkSpace().closerThan(sim.position(), 5)) {
                ArrayList<Integer> itemIndex = getHarvestItems();
                if (itemIndex.size() > 0) {
                    if (!addItemToInventory(itemIndex)) {
                        state = State.WAITING;
                    }
                } else {
                    state = State.WAITING;
                }
            } else {
                blockPos = getWorkSpace();
            }
        }
        if (state == State.WAITING) {
            if (checkForHarvestable()) {
                state = State.HARVESTING;
                getNextHarvestable();
            } else if (!isTilled()) {
                if (!hasSeeds()) {
                    getSeeds();
                }
                if (hasSeeds()) {
                    state = State.PLANTING;
                    seekUntilled();
                } else {
                    //no seeds sadge
                }
            } else {
                //sim.getJob().setState(Activity.FORCE_STOP);
            }
            if (row > depth) {
                state = State.WAITING;
            }
        } else {
            delay--;
        }
    }

    private boolean hasSeeds() {
        int amount = 0;
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getCloneItemStack(world, getWorkSpace(), world.getBlockState(getWorkSpace()));
        for (int i = 0; i < sim.getInventory().mainInventory.size(); i++) {
            ItemStack stack = sim.getInventory().getItem(i);
            if (stack.getItem() == seed.getItem()) {
                amount += stack.getCount();
            }
        }
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

    private void getSeeds() {
        int needed = farmerTile.getWidth() * farmerTile.getDepth();
        ItemStack seed = ((CropsBlock) (farmerTile.getSeed().getBlock())).getCloneItemStack(world, getWorkSpace(), world.getBlockState(getWorkSpace()));
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

    public boolean isTilled() {
        for (int i = 0; i < width * depth; i++) {
            if (HOE_LOOKUP.get(world.getBlockState(farmerTile.getMarker().below().relative(dir, (i / width) + 1).relative(dir.getClockWise(), (i % width) + 1)).getBlock()) != null) {
                return false;
            }
        }
        return true;
    }

    public void seekUntilled() {
        for (int i = 0; i < width * depth; i++) {
            if (HOE_LOOKUP.get(world.getBlockState(farmerTile.getMarker().below().relative(dir, (i / width) + 1).relative(dir.getClockWise(), (i % width) + 1)).getBlock()) != null) {
                row = i / width;
                column = i % width;
                setDestination();
                return;
            }
        }
    }

    private boolean findNextTarget() {
        if (column >= width) {
            column = 0;
            row++;
        } else {
            column++;
        }
        if (row >= depth) {
            column = 0;
            row = 0;
            return false;
        }
        return true;
    }

    private boolean getNextHarvestable() {
        BlockPos pos = null;
        ArrayList<BlockPos> harvestable = BlockPos.betweenClosedStream(farmerTile.getMarker().relative(dir, 1).relative(dir.getClockWise(), 1), farmerTile.getMarker().relative(dir, depth + 1).relative(dir.getClockWise(), width + 1))
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

    private ArrayList<Integer> getHarvestItems() {
        ArrayList<Integer> harvestItems = new ArrayList<>();
        SimInventory inventory = sim.getInventory();

        if (inventory != null) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                CompoundNBT nbt = stack.getTag();
                if (nbt != null) {
                    if (stack.getTag().contains("harvested")) {
                        harvestItems.add(i);
                    }
                }
            }

        }

        return harvestItems;
    }

    private boolean addItemToInventory(ArrayList<Integer> itemsIndex) {
        ItemStack stack = sim.getInventory().getItem(itemsIndex.get(0));

        if (stack != null) {
            if (stack.getTag() != null) {
                stack.getTag().remove("harvested");
            }
            sim.getInventory().removeItemNoUpdate(itemsIndex.get(0));
            for (BlockPos pos : chests) {
                ChestTileEntity tileEntity = (ChestTileEntity) world.getBlockEntity(pos);
                if (tileEntity != null) {
                    for (int i = 0; i < tileEntity.getContainerSize(); i++) {
                        int count = stack.getCount();
                        if (tileEntity.getItem(i).getItem() == stack.getItem()) {
                            ItemStack chestStack = tileEntity.getItem(i);
                            if (chestStack.getCount() < chestStack.getMaxStackSize()) {
                                if (chestStack.getCount() + count >= chestStack.getMaxStackSize()) {
                                    int difference = chestStack.getMaxStackSize() - chestStack.getCount();
                                    chestStack.grow(difference);
                                    count -= difference;
                                    stack.shrink(difference);
                                } else {
                                    chestStack.grow(count);
                                    count = 0;
                                    stack.shrink(stack.getCount());
                                }
                                if (count <= 0) {
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

    public void setTilled(boolean tilled) {
        this.tilled = tilled;
    }

    protected boolean isValidTarget(IWorldReader worldIn, BlockPos pos) {
        return sim.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < acceptedDistance();
    }

    public double acceptedDistance() {
        return 2.0d;
    }

    private enum State {
        RESOURCES,
        PLANTING,
        HARVESTING,
        STORING,
        WAITING


    }


}
