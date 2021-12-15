package com.resimulators.simukraft.common.jobs.reworked;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.goals.BaseGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class JobMiner implements IReworkedJob {
    private static final DirectionProperty FACING = HorizontalBlock.FACING;
    private final SimEntity sim;
    private final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    private BlockPos markerPos;
    private BlockPos offset;
    private BlockPos minepos;
    private int width; // how long it is to the left of the block
    private int depth; // how long it is to front of the block
    private int height; // how high it starts from above bedrock
    private Direction dir;
    private int delay = 20;
    private int column = 0; // used as X
    private int row = 0; // used as y
    private int layer = 0; // used as what current depth the
    private Task currentTask = Task.NONE;
    private BlockPos blockPos = null;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private Activity state = Activity.NOT_WORKING;
    //specific to the miner
    private boolean finished;
    private boolean waitingForStairs= false;

    public JobMiner(SimEntity sim) {
        this.sim = sim;
    }

    @Override
    public Activity getActivity() {
        return state;
    }

    @Override
    public void setActivity(Activity activity) {
        this.state = activity;
    }

    @Override
    public Profession jobType() {
        return Profession.MINER;
    }

    @Override
    public int intervalTime() {
        return 1000;
    }

    @Override
    public int workTime() {
        return 12000;
    }

    @Override
    public int maximumWorkPeriods() {
        return -1;
        //negative one so that it can work as much as it can. builder should work all day.
        // if it can't find resources it take a 1000 tick break
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
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.asLong());
        }
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
            if (list.contains("finished")) {
                finished = list.getBoolean("finished");
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
        return 0.3d;
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
        this.sim.getNavigation().stop();
        TileMiner miner = ((TileMiner) sim.level.getBlockEntity(getWorkSpace()));
        markerPos = miner.getMarker();
        dir = miner.getDir();
        offset = BlockPos.ZERO.relative(dir).relative(dir.getClockWise()).offset(0, -1, 0);
        width = miner.getWidth() - 1;
        depth = miner.getDepth() - 1;
        height = miner.getYpos() - 1; // height from bedrock / y = 0
        if (!sim.getInventory().hasItemStack(new ItemStack(Items.DIAMOND_PICKAXE)))
            sim.addItemStackToInventory(new ItemStack(Items.DIAMOND_PICKAXE));
        sim.setActivity(Activity.WORKING);
    }

    @Override
    public void tick() {
        if (blockPos != null && !sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 2))
            sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
        World world = sim.getCommandSenderWorld();
        if (sim.getActivity() == Activity.WORKING) { // checks if the miner should be working
            if (waitingForStairs) {
                if (placeStairs(minepos)) {
                    waitingForStairs = false;
                } else
                    return;
            }
            switch (currentTask) {
                case MINING: {
                    this.sim.getLookControl().setLookAt(Vector3d.atCenterOf(minepos));
                    this.sim.swing(Hand.MAIN_HAND, true);
                    if (delay <= 0) {// checks if the miner should be mining or doing a different task
                        ItemStack tool = sim.getMainHandItem();

                        BlockState state = sim.getCommandSenderWorld().getBlockState(minepos);
                        Block block = state.getBlock();
                        //get distance from current block targeted to be mined.
                        if (block.isAir(state, world, minepos) || sim.position().distanceTo(new Vector3d(minepos.getX(), minepos.getY(), minepos.getZ())) < 6) {
                            if (block == Blocks.BEDROCK || minepos.getY() <= 1) {
                                currentTask = Task.RETURNING;
                            } else {
                                currentTask = Task.TRAVELING;
                            }

                            if (!block.isAir(state, world, minepos) && sim.level.getBlockState(minepos).getBlock() != Blocks.OAK_STAIRS && sim.level.getBlockState(minepos).getBlock() != Blocks.OAK_SLAB) {
                                world.levelEvent(2001, minepos, Block.getId(state));
                                world.setBlock(minepos, Blocks.AIR.defaultBlockState(), 3);
                                delay = Math.min((int) (10 * state.getDestroySpeed(null, null)), 75);
                                LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld())
                                        .withRandom(world.random)
                                        .withParameter(LootParameters.TOOL, tool)
                                        .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(minepos))
                                        .withOptionalParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(minepos));
                                List<ItemStack> drops = block.defaultBlockState().getDrops(builder);

                                for (ItemStack stack : drops) {
                                    sim.getInventory().addItemStackToInventory(stack);
                                }
                                SavedWorldData.get(world).getFactionWithSim(sim.getUUID()).subCredits(getWage());

                            }

                            if (!placeStairs(minepos)) {
                                waitingForStairs = true;
                                break;
                            } else waitingForStairs = false;

                            //added mined block to inventory
                            column++;

                            if (sim.getInventory().getFirstEmptyStack() == -1) {
                                currentTask = Task.RETURNING;
                            }
                        }
                    } else {
                        delay--;
                    }
                    break;
                }
                // traveling is used when is to far away from a block and needs to move closer to it
                case TRAVELING: {
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

                    minepos = minepos.relative(dir, row).relative(dir.getClockWise(), column).relative(Direction.DOWN, layer);
                    blockPos = minepos;
                    if (minepos.closerThan(sim.position(), 5)) {
                        currentTask = Task.MINING;
                    } else {
                        blockPos = minepos;
                    }

                    while (sim.getCommandSenderWorld().getBlockState(minepos).getBlock().isAir(sim.getCommandSenderWorld().getBlockState(minepos), sim.getCommandSenderWorld(), minepos) || sim.level.getBlockState(minepos).getBlock() == Blocks.OAK_STAIRS || sim.level.getBlockState(minepos).getBlock() == Blocks.OAK_SLAB && layer < depth) {
                        if (!placeStairs(minepos)) {
                            waitingForStairs = true;
                            break;
                        } else waitingForStairs = false;

                        minepos = offset;
                        minepos = minepos.offset(markerPos.getX(), markerPos.getY(), markerPos.getZ());

                        column++;

                        if (column >= width) {
                            column = 0;
                            row++;
                        }
                        if (row >= depth) {
                            row = 0;
                            layer++;
                        }

                        minepos = minepos.relative(dir, row).relative(dir.getClockWise(), column).relative(Direction.DOWN, layer);
                        blockPos = minepos;
                        if (minepos.closerThan(sim.position(), 5)) {
                            currentTask = Task.MINING;
                        } else {
                            blockPos = minepos;
                        }
                    }
                    break;
                }
                // returning to base to empty inventory
                case RETURNING: {
                    BlockPos pos = sim.getJob().getWorkSpace();
                    if ((sim.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 5)) {
                        if (Utils.getInventoryAroundPos(sim.getJob().getWorkSpace(), sim.level) != null) {
                            if (Utils.addSimInventoryToChest(Utils.getInventoryAroundPos(sim.getJob().getWorkSpace(), sim.level), sim)) {
                                sim.getJob().setActivity(Activity.NOT_WORKING);
                                currentTask = Task.NONE;
                                int id = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).getId();
                                sim.fireSim(sim, id, false);
                            } else {
                                SimuKraft.LOGGER().debug("Unable to move Items into chest");
                                SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(String.format("Sim %s in unable to empty its invetory into a chest at %s", sim.getDisplayName().getString(), sim.getJob().getWorkSpace().toString()), sim.level);
                            }
                        }
                    } else {
                        blockPos = sim.getJob().getWorkSpace();
                    }
                    break;
                }
                case NONE:
                    currentTask = Task.TRAVELING;
                    break;
            }
        }
        Faction faction = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID());
        PlayerEntity player = world.getPlayerByUUID(faction.getOnlineFactionPlayer());
        // debugging \/
        if (player != null) {
            player.displayClientMessage(new StringTextComponent("Working at: " + minepos + "; and navigator set at: " + sim.getNavigation().getTargetPos() + "; inventory state: " + (sim.getInventory().getFirstEmptyStack() == -1 ? "Full" : "Not Full")), true);
        }
    }

    /**
     * Used to check where the next stair needs to be placed and places it. does not handle rotation handles placing
     * slabs and the corners.
     */
    private boolean placeStairs(BlockPos pos) {
        if (row == 0 && column != 0) {
            if (0 == (layer - column + 1) % (2 * (depth - 2) + 2 * (width - 2)))
                if (column == width - 1) {
                    return placeBlock(pos, Blocks.OAK_SLAB);
                } else {
                    return placeBlock(pos, Blocks.OAK_STAIRS, dir.getCounterClockWise());
                }
        }
        if (column == width - 1 && row != 0) {
            if (0 == (1 + layer - row - (width - 2)) % (2 * (depth - 2) + 2 * (width - 2)))
                if (row == depth - 1) {
                    return placeBlock(pos, Blocks.OAK_SLAB);
                } else {
                    return placeBlock(pos, Blocks.OAK_STAIRS, dir.getCounterClockWise().getCounterClockWise());
                }
        }
        if (row == depth - 1 && column != width - 1) {
            if (0 == (layer + column - 2 * (width - 2) - (depth - 2)) % (2 * (depth - 2) + 2 * (width - 2)))
                if (column == 0) {
                    return placeBlock(pos, Blocks.OAK_SLAB);
                } else {
                    return placeBlock(pos, Blocks.OAK_STAIRS, dir.getCounterClockWise().getCounterClockWise().getCounterClockWise());
                }
        }
        if (column == 0 && row != depth - 1) {
            if (0 == (layer + row) % (2 * (depth - 2) + 2 * (width - 2)))
                if (row == 0) {
                    return placeBlock(pos, Blocks.OAK_SLAB);
                } else {
                    return placeBlock(pos, Blocks.OAK_STAIRS);
                }
        }
        return true;
    }

    private boolean placeBlock(BlockPos pos, Block block) {
        return placeBlock(pos, block, dir);
    }

    private boolean placeBlock(BlockPos pos, Block block, Direction orientation) {
        if (hasBlock(block)) {
            consumeBlock(block);
            if (block == Blocks.OAK_STAIRS) { // checks if it should be a stair to be placed
                sim.level.setBlockAndUpdate(pos, block.defaultBlockState().setValue(FACING, orientation));
            } else if (block == Blocks.OAK_SLAB) { // the block to be placed is a slab
                sim.level.setBlockAndUpdate(pos, block.defaultBlockState().setValue(TYPE, SlabType.TOP));
            }
            return true;
        }
        return false;
    }

    private boolean hasBlock(Block block) {
        if (sim.getInventory().hasItemStack(new ItemStack(block)))
            return true;
        for (BlockPos pos : BaseGoal.findChestsAroundTargetBlock(workSpace, 1, sim.level)) {
            ChestTileEntity chest = (ChestTileEntity) sim.level.getBlockEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    if (chest.getItem(i).sameItem(new ItemStack(block)))
                        return true;
                }
            }
        }
        return false;
    }

    private void consumeBlock(Block block) {
        if (sim.getInventory().hasItemStack(new ItemStack(block))) {
            for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
                if (sim.getInventory().getItem(i).sameItem(new ItemStack(block))) {
                    sim.getInventory().getItem(i).shrink(1);
                    return;
                }
            }
        }
        for (BlockPos pos : BaseGoal.findChestsAroundTargetBlock(workSpace, 1, sim.level)) {
            ChestTileEntity chest = (ChestTileEntity) sim.level.getBlockEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    if (chest.getItem(i).sameItem(new ItemStack(block))) {
                        chest.getItem(i).shrink(1);
                        return;
                    }
                }
            }
        }
    }

    enum Task {
        MINING,
        RETURNING,
        WAITING,
        TRAVELING,
        NONE
    }
}
