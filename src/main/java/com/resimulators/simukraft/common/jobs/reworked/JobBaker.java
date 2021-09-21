package com.resimulators.simukraft.common.jobs.reworked;

import com.resimulators.simukraft.common.entity.goals.BakerGoal;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import com.resimulators.simukraft.utils.Utils;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JobBaker implements IReworkedJob {
    private final SimEntity sim;
    private final World world;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final ArrayList<SimEntity> simFarmers = new ArrayList<>();
    private final ArrayList<BlockPos> farmerWorkSpace = new ArrayList<>();
    private final HashMap<BlockPos, ArrayList<BlockPos>> farmChestsHashMap = new HashMap<>();
    private int periodsworked = 0;
    private BlockPos workSpace, blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private boolean finished = false;
    private State state = State.WAITING;
    private int tick, delay = 0, navigation, wheatAmount;
    private boolean validateSentence = false;


    public JobBaker(SimEntity simEntity) {
        this.sim = simEntity;
        world = sim.level;

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
        return Profession.BAKER;
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
        nbt.add(data);
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
        sim.setItemInHand(sim.getUsedItemHand(), Items.BREAD.getDefaultInstance());
        blockPos = getWorkSpace();
        if (!chests.isEmpty()) {
            state = State.CHEST_INTERACTION;
        } else if (!validateWorkArea()) {
            sim.getJob().setActivity(Activity.NOT_WORKING);
        }
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        if (getWorkSpace() != null) {
            if (chests.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobBaker) has no inventory at: " + getWorkSpace(), world);
            } else {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobBaker) has started working", world);
                validateSentence = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {

        tick++;
        delay++;
        navigation++;
        Utils.getInventoryAroundPos(getWorkSpace(), world);
        putSimInvInChest();
        if (!validateWorkArea()) {
            validateSentence = false;
            sim.getJob().setActivity(Activity.NOT_WORKING);
        } else if (validateWorkArea()) {
            sim.getJob().setActivity(Activity.WORKING);
        }
        if (delay >= 20 * 5) { // For now it's 100 (20*5) to for testing purposes, will be changed later on
            state = State.CHECKING_WHEAT;
            wheatAmount = 0;
            checkForWheat();
            state = State.PRODUCING_BREAD;
            if (wheatAmount >= 3) {
                wheatAmount = Math.min(wheatAmount, 192);
                produceBread();
            }
            delay = 0;
        }
        if (navigation >= 20 * 8 && isEmptyChest()) {
            if (farmerWorkSpace.isEmpty()) {
                getSimFarmers();
                getFarmWorkSpace();
                getFarmChests();
            }
            navigation = 0;
        }
        if (!farmerWorkSpace.isEmpty()) {
            state = State.NAVIGATING;
            blockPos = farmerWorkSpace.get(0);
            sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
            if (sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 3)) {
                state = State.COLLECTING_WHEAT;
                for (BlockPos workChests : farmChestsHashMap.get(blockPos)) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(workChests);
                    if (chestTileEntity != null) {
                        int simInvLeftOver;
                        int farmChestCount = Math.min(chestTileEntity.countItem(Items.WHEAT), 192);
                        int simInvFarmStack = farmChestCount / 64;
                        if (simInvFarmStack >= 1) simInvLeftOver = farmChestCount - (simInvFarmStack * 64);
                        else {
                            simInvFarmStack = 0;
                            simInvLeftOver = farmChestCount;
                        }
                        for (int i = 0; i < chestTileEntity.getContainerSize(); i++) {
                            ItemStack wheatFarmStack = chestTileEntity.getItem(i);
                            int wheatFarmAmount = wheatFarmStack.getCount();
                            if (wheatFarmStack.getItem().equals(Items.WHEAT)) {
                                if (farmChestCount >= wheatFarmAmount) {
                                    farmChestCount -= wheatFarmAmount;
                                    chestTileEntity.setItem(i, ItemStack.EMPTY);
                                } else {
                                    int chestLeftOver = wheatFarmAmount - farmChestCount;
                                    chestTileEntity.setItem(i, new ItemStack(Items.WHEAT, chestLeftOver));
                                    farmChestCount = 0;
                                }
                            }
                            if (farmChestCount == 0) break;
                        }
                        for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
                            ItemStack simInvStack = sim.getInventory().getItem(i);
                            if (simInvStack.isEmpty()) {
                                if (simInvFarmStack != 0) {
                                    simInvFarmStack--;
                                    sim.getInventory().setItem(i, new ItemStack(Items.WHEAT, 64));
                                } else {
                                    sim.getInventory().setItem(i, new ItemStack(Items.WHEAT, simInvLeftOver));
                                    break;
                                }
                            }
                        }
                    }
                    if (checkSimFull()) {
                        break;
                    }
                }
                if (checkSimFull()) {
                    state = State.NAVIGATING_BACK;
                    blockPos = sim.getJob().getWorkSpace();
                    farmerWorkSpace.clear();
                } else {
                    farmerWorkSpace.remove(0);
                    if (farmerWorkSpace.isEmpty()) {
                        blockPos = sim.getJob().getWorkSpace();
                        state = State.NAVIGATING_BACK;
                    }
                }
            }
        }
        if (farmerWorkSpace.isEmpty() && state.equals(State.NAVIGATING_BACK)) {
            sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
            if (sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 1)) {
                state = State.CHECKING_WHEAT;
            }
        }
    }


    private void putSimInvInChest() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        int chestIndex = 0;
        while (sim.getInventory().getSlotFor(new ItemStack(Items.WHEAT)) != -1){
            int index = sim.getInventory().getSlotFor(new ItemStack(Items.WHEAT));
            BlockPos entityPos = chests.get(chestIndex);
            ChestTileEntity entity = (ChestTileEntity) world.getBlockEntity(entityPos);
            if (entity == null){
                chestIndex += 1;
                if (chestIndex > chests.size()){
                    break;
                }
                continue;
            }
            InvWrapper wrapper = new InvWrapper(entity);
            ItemStack stack = sim.getInventory().getItem(index);
            if(ItemHandlerHelper.insertItemStacked(wrapper,stack,false) == ItemStack.EMPTY){
                sim.getInventory().setItem(index,ItemStack.EMPTY);
            }else{
                faction.sendFactionChatMessage("Baker " + sim.getName() + ", has ran out of inventory space",world);

            }

        }
    }

    private void checkForWheat() {
        if (state == State.CHECKING_WHEAT) {
            for (BlockPos chest : chests) {
                ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(chest);
                if (chestTileEntity != null) {
                    wheatAmount += chestTileEntity.countItem(Items.WHEAT);
                }
            }
        }
    }

    private void produceBread() {
        int bread = wheatAmount / 3;
        int wheatToBeRemoved = bread * 3;
        chestLoop:
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(chest);
            if (chestTileEntity != null) {
                for (int i = chestTileEntity.getContainerSize() - 1; i >= 0; i--) {
                    ItemStack wheatStack = chestTileEntity.getItem(i);
                    int wheatCount = wheatStack.getCount();
                    if (wheatStack.getItem().equals(Items.WHEAT)) {
                        if (wheatToBeRemoved >= 64) {
                            wheatToBeRemoved -= wheatCount;
                            chestTileEntity.removeItemNoUpdate(i);
                        } else if (wheatToBeRemoved == 0) {
                            break;
                        } else {
                            if (wheatToBeRemoved >= wheatCount) {
                                wheatToBeRemoved -= wheatCount;
                                chestTileEntity.removeItemNoUpdate(i);
                            } else {
                                int wheatLeft = wheatCount - wheatToBeRemoved;
                                ItemStack newWheatStack = new ItemStack(Items.WHEAT, wheatLeft);
                                chestTileEntity.setItem(i, newWheatStack);
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < chestTileEntity.getContainerSize(); i++) {
                    if (chestTileEntity.getItem(i).isEmpty()) {
                        chestTileEntity.setItem(i, new ItemStack(Items.BREAD, bread));
                        break chestLoop;
                    }
                }
            }

        }
    }

    private boolean isEmptyChest() {
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(chest);
            if (chestTileEntity != null) {
                if (chestTileEntity.countItem(Items.BREAD) == 0) return true;
            }
        }
        return false;
    }

    private void getSimFarmers() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        ArrayList<UUID> simFarmersUUID = faction.getEmployedSims();
        ServerWorld serverWorld = (ServerWorld) this.world;
        for (UUID simFarmerUUID : simFarmersUUID) {
            SimEntity simEntity = (SimEntity) serverWorld.getEntity(simFarmerUUID);
            if (simEntity != null) {
                if (simEntity.getJob().jobType().equals(Profession.FARMER) && !simFarmers.contains(simEntity)) {
                    simFarmers.add(simEntity);
                }
            }
        }
    }

    private void getFarmWorkSpace() {
        for (SimEntity simFarmer : simFarmers) {
            JobFarmer jobFarmer = (JobFarmer) simFarmer.getJob();
            if (jobFarmer != null) {
                TileFarmer tileFarmer = (TileFarmer) world.getBlockEntity(jobFarmer.getWorkSpace());
                if (tileFarmer != null) {
                    if (tileFarmer.getSeed().equals(Seed.WHEAT) && !farmerWorkSpace.contains(jobFarmer.getWorkSpace()) && tileHasWheat(jobFarmer.getWorkSpace())) {
                        farmerWorkSpace.add(jobFarmer.getWorkSpace());
                    }
                }
            }
        }
    }

    private void getFarmChests() {
        for (BlockPos farmBox : farmerWorkSpace) {
            ArrayList<BlockPos> chestArrayList = new ArrayList<>();
            ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundAndBelowPosition(farmBox, 5);
            for (BlockPos blockPos : blockPosArrayList) {
                if (world.getBlockEntity(blockPos) instanceof ChestTileEntity) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(blockPos);
                    if (chestTileEntity != null) {
                        if (chestTileEntity.countItem(Items.WHEAT) > 0) {
                            chestArrayList.add(blockPos);
                        }
                    }
                }
            }
            farmChestsHashMap.put(farmBox, chestArrayList);
        }
    }

    private boolean checkSimFull() {
        return sim.getInventory().countItem(Items.WHEAT) >= 192;
    }

    private boolean tileHasWheat(BlockPos workSpace) {
        ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundPosition(workSpace, 5);
        for (BlockPos blockPos : blockPosArrayList) {
            if (world.getBlockEntity(blockPos) instanceof ChestTileEntity) {
                ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(blockPos);
                if (chestTileEntity != null) {
                    if (chestTileEntity.countItem(Items.WHEAT) > 0) return true;
                }
            }
        }
        return false;
    }

    private enum State {
        WAITING,
        CHECKING_WHEAT,
        PRODUCING_BREAD,
        CHEST_INTERACTION,
        NAVIGATING,
        NAVIGATING_BACK,
        COLLECTING_WHEAT
    }


}