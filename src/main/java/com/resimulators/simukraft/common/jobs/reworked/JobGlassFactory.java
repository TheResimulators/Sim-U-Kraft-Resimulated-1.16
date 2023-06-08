package com.resimulators.simukraft.common.jobs.reworked;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.entity.sim.SimInventory;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JobGlassFactory implements IReworkedJob {
    private final SimEntity sim;
    private final World world;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private ArrayList<BlockPos> furnaces = new ArrayList<>();
    private final ArrayList<Integer> itemsToBeMoved = new ArrayList<>();
    private int periodsworked = 0;
    private BlockPos workSpace, blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private boolean finished;
    private int tick;
    private int delay = 20;
    private State state = State.WAITING;
    private BlockPos targetPos;
    private boolean collected = false;
    //to make interaction happen over multiple ticks. keeps track of current furnace/chest
    private int furnaceIndex;
    private int chestIndex;

    private int waitingDelay = 120;
    private int waitingTick = 0;

    public JobGlassFactory(SimEntity simEntity) {
        this.sim = simEntity;
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
        return Profession.GLASS_FACTORY;
    }

    @Override
    public int intervalTime() {
        return 400;
    }

    @Override
    public int workTime() {
        return 10000;
    }

    @Override
    public int maximumWorkPeriods() {
        return 3;
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
        nbt.add(other);
        other.putBoolean("finished", finished);
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
        return 0.7d;
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
        sim.setItemInHand(sim.getUsedItemHand(), Items.DIAMOND_SHOVEL.getDefaultInstance());
        chests = Utils.findInventoriesAroundPos(getWorkSpace(),10,world);
        furnaces = findFurnacesAroundPos(getWorkSpace(),10);
        sim.setActivity(Activity.WORKING);
        if (!furnaces.isEmpty()) {
            blockPos = furnaces.get(0);
            ValidateFurnaces();
            state = State.COLLECT_FINISHED_PRODUCT;
            sim.setStatus("Checking Furnaces");
        } else {
            if (!validateWorkArea()) {
                sim.getJob().setActivity(Activity.NOT_WORKING);
            }


        }
    }

    @Override
    public void tick() {
        if (blockPos != null && !sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 2)){
            sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
        }
        chests = Utils.findInventoriesAroundPos(getWorkSpace(), 5, world);
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (sim.blockPosition().closerThan(new Vector3d(getWorkSpace().getX(), getWorkSpace().getY(), getWorkSpace().getZ()), 5)) {
                if (validateWorkArea()) {
                    sim.setActivity(Activity.WORKING);
                } else {
                    state = State.WAITING;
                }
            }
        }


        if (delay <= 0) {
            delay = 10;
            if (state == State.CHECK_FOR_SAND)
            {
                int targetAmount = furnaces.size() * 64;
                int found = 0;

                for (int i = 0; i< furnaces.size();i++) {
                    found += getSandFromChest(i,targetAmount);
                    targetAmount -= found;
                    if (targetAmount <= 0)break;
                }
                state = State.SMELTING;
                return;



            }
            if (state == State.COLLECT_FINISHED_PRODUCT) {
                if (interactWithFurnace()) {
                    state = State.PUT_AWAY_RESOURCES;
                    getItemsToMove();
                    ValidateChests();
                    if (!chests.isEmpty()) {
                        blockPos = chests.get(0);
                    } else {
                        if (!validateWorkArea()) {
                            sim.getJob().setActivity(Activity.NOT_WORKING);
                        }
                    }
                    return;
                }
            }
            if (state == State.PUT_AWAY_RESOURCES) {
                sim.setStatus("Checking Chests");
                if (!collected) { // just started shift
                    getItemsToMove(); // gets glass to put away
                    if (emptyInventory()) {
                        if (getSandStock() < 64){
                            state = State.TRAVELING;
                            BlockPos sand = findSand();
                            if (sand != BlockPos.ZERO) {
                                targetPos = sand;
                                blockPos = sand;
                            }
                            return;
                        }else
                        {
                            state = State.WAITING;
                            waitingTick = 0;
                            return;
                        }
                    }
                } else {
                    getItemsToMove();
                    getSandToMove();
                    if (emptyInventory()) {
                        state = State.TRAVELING;
                        BlockPos sand = findSand();
                        if (sand != BlockPos.ZERO) {
                            targetPos = sand;
                            blockPos = sand;
                        }
                        return;
                    }
                }
            }
            if (state == State.TRAVELING) {
                sim.setStatus("Traveling");
                if (targetPos != null) {
                    if (targetPos.closerThan(sim.position(), 6)) {
                        state = State.COLLECTING;
                        return;
                    } else {
                        blockPos = targetPos;
                    }
                } else {
                    BlockPos sand = findSand();
                    if (sand != BlockPos.ZERO) {
                        targetPos = sand;
                        blockPos = sand;
                    }else
                    {
                        sim.setActivity(Activity.NOT_WORKING);
                        sim.setStatus("Finished work");
                        finishedWorkPeriod();
                        Faction faction = SavedWorldData.get(sim.getCommandSenderWorld()).getFactionWithSim(sim.getUUID());
                        faction.sendFactionChatMessage(sim.getName().getString() + " Glass Factory Worker can't find any sand at " + sim.getJob().getWorkSpace(),sim.getCommandSenderWorld());
                        if (getPeriodsWorked() >= maximumWorkPeriods())
                        {
                            faction.sendFactionChatMessage(sim.getName().getString() + " Glass Factory Worker has finished work for the day at " + sim.getJob().getWorkSpace(),sim.getCommandSenderWorld());
                        }

                    }
                }
            }
            if (state == State.COLLECTING) {
                sim.setStatus("Collecting sand");
                if (targetPos != null) {
                    if (targetPos.closerThan(sim.position(), 6)) {
                        if (world.getBlockState(targetPos).getBlock() == Blocks.SAND) {
                            addItemToInventoryFromWorld(targetPos);
                            world.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                            if (!findNextSand()) {
                                state = State.RETURNING;
                                blockPos = getWorkSpace();
                                targetPos = null;
                                collected = true;
                                return;
                            }
                        } else {
                            findNextSand();
                        }
                    } else {
                        blockPos = targetPos;
                        state = State.TRAVELING;
                        return;
                    }
                } else {
                    state = State.TRAVELING;
                }
            }
            if (state == State.RETURNING) {
                sim.setStatus("Returning to base");
                if (sim.distanceToSqr(getWorkSpace().getX(), getWorkSpace().getY(), getWorkSpace().getZ()) < 4) {
                    state = State.SMELTING;
                    return;
                }
            }
            if (state == State.SMELTING) {
                sim.setStatus("Smelting Glass");
                ArrayList<Integer> stacks = getSandInventory();
                 if (stacks.size() > 0) {
                    int index = getBurnables();
                    if (index >= 0 || !checkFuelStatus()) {
                        if (!smeltSand(stacks, index)) {
                            state = State.COLLECT_FINISHED_PRODUCT;
                            getSandToMove();
                            getItemsToMove();
                        }

                    } else {
                        state = State.COLLECT_FINISHED_PRODUCT;
                        getSandToMove();
                        getItemsToMove();
                    }
                }else{
                    getItemsToMove();
                    state = State.PUT_AWAY_RESOURCES;

                }

            }
            if (state == State.NOTHING) {
                state = State.COLLECT_FINISHED_PRODUCT;
                blockPos = furnaces.get(0);
            }
            if (state == State.WAITING)
            {
                if (waitingTick >= waitingDelay)
                {
                    state = State.CHECK_FOR_SAND;
                }else
                {
                    waitingTick++;
                }

            }

        } else {
            delay--;
        }
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        boolean valid = true;
        if (getWorkSpace() != null) {
            if (chests.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) has no Inventory at " + getWorkSpace(), world);
                valid = false;
            } else if (furnaces.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) Has no Furnaces to smelt with at " + getWorkSpace(), world);
                valid = false;
            } else {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) has started work at " + getWorkSpace(), world);
            }
        } else {
            return false;
        }
        return valid;
    }

    private boolean interactWithFurnace() {
        BlockPos furnace = furnaces.get(furnaceIndex);
        if (world.getBlockEntity(furnace) instanceof FurnaceTileEntity) {
            FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getBlockEntity(furnace);
            if (tileEntity != null) {
                ItemStack stack = tileEntity.getItem(2);
                if (stack != ItemStack.EMPTY) {
                    tileEntity.setItem(2, ItemStack.EMPTY);
                    sim.getInventory().addItemStackToInventory(stack);
                }
            }
        }

        if (furnaceIndex >= furnaces.size() - 1) {
            furnaceIndex = 0;
            return true;

        } else {
            furnaceIndex++;
        }
        return false;
    }

    private void getItemsToMove() {

        for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
            ItemStack stack = sim.getInventory().getItem(i);
            if (stack.getItem() == Items.GLASS) {
                itemsToBeMoved.add(i);
            }

        }

    }

    private void ValidateChests() {
        ArrayList<BlockPos> chestsToBeRemoved = new ArrayList<>();
        for (BlockPos chest : chests) {
            if (world.getBlockEntity(chest) instanceof FurnaceTileEntity) {
                FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getBlockEntity(chest);
                if (tileEntity == null) {
                    chestsToBeRemoved.add(chest);
                }
            }

        }
        for (BlockPos chest : chestsToBeRemoved) {
            chests.remove(chest);
        }
    }

    private int getSandStock()
    {
        int sandCount = 0;
        for(BlockPos chestPos: chests)
        {
            ChestTileEntity chestEntity = (ChestTileEntity) world.getBlockEntity(chestPos);
            if (chestEntity != null)
            {
                sandCount += chestEntity.countItem(Items.SAND);
            }
        }



        return sandCount;
    }


    private int getSandFromChest(int chestIndex,int targetAmount)
    {
        int sandCount = 0;
        ChestTileEntity chestEntity = (ChestTileEntity) world.getBlockEntity(chests.get(chestIndex));
        if (chestEntity != null) {
            for (int i = 0; i < chestEntity.getContainerSize(); i++) {
                if (chestEntity.getItem(i).getItem() == Items.SAND)
                {
                    sim.getInventory().addItemStackToInventory(chestEntity.getItem(i));
                    sandCount = chestEntity.getItem(i).getCount();
                    chestEntity.setItem(i,ItemStack.EMPTY);
                    if (sandCount > targetAmount) return sandCount;
                }
            }
        }

        return sandCount;
    }

    private boolean emptyInventory() { // used directly after furnace interaction to empty the collected glass into chests
        BlockPos chest = chests.get(chestIndex);
        ItemStack newItem;
        ItemStack newChestStack;
        while (itemsToBeMoved.size() > 0) {
            if (world.getBlockEntity(chest) instanceof ChestTileEntity) {
                ChestTileEntity chestEntity = (ChestTileEntity) world.getBlockEntity(chest);
                if (chestEntity != null) {
                    for (int i = 0; i < chestEntity.getContainerSize(); i++) {
                        ItemStack item = sim.getInventory().getItem(itemsToBeMoved.get(0));
                        ItemStack chestStack = chestEntity.getItem(i);
                        if (chestStack == ItemStack.EMPTY) {
                            chestEntity.setItem(i, item);
                            sim.getInventory().setItem(itemsToBeMoved.get(0), ItemStack.EMPTY);

                        } else if (chestStack.getItem() == item.getItem()) {
                            int chestSize = chestStack.getCount();
                            int itemSize = item.getCount();
                            int chestSpace = chestStack.getMaxStackSize() - chestSize;
                            if (itemSize > chestSpace) {
                                newItem = item.copy();
                                newItem.shrink(chestSpace);
                                newChestStack = chestStack.copy();
                                newChestStack.grow(chestSpace);
                            } else {
                                newItem = ItemStack.EMPTY;
                                newChestStack = chestStack.copy();
                                newChestStack.grow(itemSize);
                            }
                            chestEntity.setItem(i, newChestStack);
                            sim.getInventory().setItem(itemsToBeMoved.get(0), newItem);
                        }


                    }
                    itemsToBeMoved.remove(0);
                }
            }
            return false;
        }
        return true;

    }
    public ArrayList<BlockPos> findFurnacesAroundPos(BlockPos targetBlock, int distance) {
        ArrayList<BlockPos> blockPoses = BlockUtils.getBlocksAroundAndBelowPosition(targetBlock, distance);
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (BlockPos blockPos : blockPoses) {
            if (world.getBlockEntity(blockPos) instanceof FurnaceTileEntity) {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }

    private void ValidateFurnaces() {
        ArrayList<BlockPos> furnacesToBeRemoved = new ArrayList<>();
        for (BlockPos furnace : furnaces) {
            if (world.getBlockEntity(furnace) instanceof FurnaceTileEntity) {
                FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getBlockEntity(furnace);
                if (tileEntity == null) {
                    furnacesToBeRemoved.add(furnace);
                }
            }
        }
        for (BlockPos furnace : furnacesToBeRemoved) {
            furnaces.remove(furnace);
        }
    }

    private BlockPos findSand() {
        final BlockPos[] sand = {BlockPos.ZERO};
        Iterable<BlockPos> blockPoses = BlockPos.betweenClosedStream(getWorkSpace().offset(-20, -3, -20), getWorkSpace().offset(20, 5, 20))
            .filter(blockPos -> world.getBlockState(blockPos).getBlock() == Blocks.SAND)
            .map(BlockPos::immutable)
            .sorted(Comparator.comparingDouble(blockPos -> getWorkSpace().distSqr(blockPos)))
            .collect(Collectors.toCollection(ArrayList::new));
        for (BlockPos blockPos : blockPoses) {
            if (world.getBlockState(blockPos).getBlock() == Blocks.SAND) {
                sand[0] = blockPos;
                break;

            }
        }

        return sand[0];
    }

    private void getSandToMove() {
        for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
            ItemStack stack = sim.getInventory().getItem(i);
            if (stack.getItem() == Items.SAND) {
                itemsToBeMoved.add(i);
            }

        }
    }

    private void addItemToInventoryFromWorld(BlockPos pos) {
        BlockState above = world.getBlockState(pos);

        if (above.getBlock() == Blocks.AIR) {
            pos = pos.above();
        }
        Block block = world.getBlockState(pos).getBlock();
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld())
            .withRandom(world.random)
            .withParameter(LootParameters.TOOL, sim.getUseItem())
            .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(pos))
            .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos));

        List<ItemStack> drops = block.defaultBlockState().getDrops(builder);

        for (ItemStack stack : drops) {
            sim.getInventory().addItemStackToInventory(stack);
        }
    }

    private boolean findNextSand() {
        BlockPos sand = BlockPos.ZERO;
        ArrayList<BlockPos> blockPoses = BlockPos.betweenClosedStream(sim.blockPosition().offset(-5, -3, -5), sim.blockPosition().offset(5, 5, 5))
            .filter(blockPos -> world.getBlockState(blockPos).getBlock() == Blocks.SAND)
            .map(BlockPos::immutable)
            .sorted(Comparator.comparingDouble(blockPos -> sim.position().distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
            .collect(Collectors.toCollection(ArrayList::new));
        if (blockPoses.size() > 0) {
            sand = blockPoses.get(0);
            blockPos = sand;
            targetPos = blockPos;
        }
        return sand != BlockPos.ZERO;
    }

    private ArrayList<Integer> getSandInventory() {
        ArrayList<Integer> sandStacks = new ArrayList<>();
        for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
            ItemStack stack = sim.getInventory().getItem(i);
            if (stack.getItem() == Items.SAND) {
                sandStacks.add(i);
            }
        }
        return sandStacks;
    }

    private int getBurnables() {
        int stack = -1;
        int stackIndex = -1;
        BlockPos chestSlot = BlockPos.ZERO;
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(pos);
            if (chest != null) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack chestStack = chest.getItem(i);
                    ItemStack currentStack;
                    if (stack != -1) {
                        currentStack = chest.getItem(stack);

                        if (ForgeHooks.getBurnTime(chestStack) > ForgeHooks.getBurnTime(currentStack) && ForgeHooks.getBurnTime(chestStack) > 0) {
                            stack = i;
                            chestSlot = pos;
                        }
                    } else {
                        if (ForgeHooks.getBurnTime(chestStack) > 0) {
                            stack = i;
                            chestSlot = pos;
                        }
                    }
                }
            }
        }
        if (chestSlot != BlockPos.ZERO) {
            ChestTileEntity chest = ((ChestTileEntity) world.getBlockEntity(chestSlot));

            if (chest != null) {
                ItemStack chestStack = chest.getItem(stack);
                while (chestStack.getCount() > 0) {
                    for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
                        if (sim.getInventory().getItem(i).isEmpty()) {
                            sim.getInventory().setItem(i, chestStack.copy());
                            chestStack.shrink(chestStack.getCount());
                            stackIndex = i;
                            break;
                        } else {
                            if (chestStack.getItem() == sim.getInventory().getItem(i).getItem() && sim.getInventory().getItem(i).getCount() < sim.getInventory().getItem(i).getMaxStackSize()) {
                                ItemStack simItem = sim.getInventory().getItem(i);
                                int space = simItem.getMaxStackSize() - simItem.getCount();
                                if (chestStack.getCount() > space) {
                                    simItem.setCount(simItem.getMaxStackSize());
                                    chestStack.shrink(space);
                                } else {
                                    simItem.grow(chestStack.getCount());
                                    chestStack.shrink(chestStack.getCount());

                                }
                                stackIndex = i;
                                break;

                            }
                        }
                    }
                }
            }

        }
        return stackIndex;
    }

    private boolean checkFuelStatus() { // return true if needs fuel
        for (BlockPos pos : furnaces) {
            FurnaceTileEntity furnace = (FurnaceTileEntity) world.getBlockEntity(pos);
            if (furnace != null) {
                if (furnace.getItem(1).isEmpty()) {
                    return true;
                }

            }

        }
        return false;


    }

    private boolean smeltSand(ArrayList<Integer> indexs, int burnable) {
        boolean success = false;
        for (BlockPos pos : furnaces) {
            if (world.getBlockEntity(pos) instanceof FurnaceTileEntity) {
                FurnaceTileEntity tile = (FurnaceTileEntity) world.getBlockEntity(pos);
                if (tile != null) {
                    if (tile.getItem(0).isEmpty()) {
                        tile.setItem(0, sim.getInventory().getItem(indexs.get(0)));
                        sim.getInventory().setItem(indexs.get(0), ItemStack.EMPTY);
                        indexs.remove(indexs.get(0));
                        success = true;
                    } else if (tile.getItem(0).getItem() == Items.SAND) {
                        ItemStack tileStack = tile.getItem(0);
                        if (tileStack.getCount() < tileStack.getMaxStackSize()) {
                            int space = tileStack.getMaxStackSize() - tileStack.getCount();
                            if (sim.getInventory().getItem(indexs.get(0)).getCount() < space) {
                                tileStack.grow(sim.getInventory().getItem(indexs.get(0)).getCount());
                                sim.getInventory().setItem(indexs.get(0), ItemStack.EMPTY);
                            } else {
                                tileStack.grow(space);
                                sim.getInventory().getItem(indexs.get(0)).shrink(space);
                            }

                            success = true;
                        }
                    }
                    if (success) {
                        if (burnable < 0) {
                            burnable = checkInventoryBurnables();
                        }
                        if (burnable >= 0) {
                            tile.setItem(1, sim.getInventory().getItem(burnable));
                            sim.getInventory().setItem(burnable, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        return success;
    }

    private int checkInventoryBurnables() {
        int stack = -1;
        SimInventory inv = sim.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack invStack = inv.getItem(i);
            ItemStack currentStack;
            if (stack != -1) {
                currentStack = inv.getItem(stack);
                if (ForgeHooks.getBurnTime(invStack) > ForgeHooks.getBurnTime(currentStack) && ForgeHooks.getBurnTime(invStack) > 0) {
                    stack = i;
                }
            } else {
                if (ForgeHooks.getBurnTime(invStack) > 0) {
                    stack = i;
                }
            }
        }
        return stack;
    }


    private enum State {
        TRAVELING,
        CHECK_FOR_SAND,
        COLLECTING,
        SMELTING,
        WAITING,
        RETURNING,
        NOTHING,
        PUT_AWAY_RESOURCES,
        COLLECT_FINISHED_PRODUCT


    }
}
