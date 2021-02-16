package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.common.jobs.JobBaker;
import com.resimulators.simukraft.common.jobs.JobFarmer;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BakerGoal extends BaseGoal<JobBaker> {

    private final SimEntity sim;
    private final World world;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final ArrayList<SimEntity> simFarmers = new ArrayList<>();
    private final ArrayList<BlockPos> farmerWorkSpace = new ArrayList<>();
    private final HashMap<BlockPos, ArrayList<BlockPos>> farmChestsHashMap = new HashMap<>();
    private State state = State.WAITING;
    private int tick, delay = 0, navigation, wheatAmount;
    private boolean validateSentence = false;

    public BakerGoal(SimEntity sim) {
        super(sim, sim.getAIMoveSpeed()*2, 20);
        this.sim = sim;
        this.world = sim.world;
    }

    @Override
    public boolean shouldExecute() {
        job = (JobBaker) sim.getJob();
        if (job == null) return  false;
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 2)) {
                sim.setActivity(Activity.WORKING);
                findChestAroundBlock(job.getWorkSpace());
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(), Items.BREAD.getDefaultInstance());
        destinationBlock = job.getWorkSpace();
        if (!chests.isEmpty()) {
            state = State.CHEST_INTERACTION;
        } else if (!validateWorkArea()) {
            sim.getJob().setState(Activity.NOT_WORKING);
        }
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
        delay++;
        navigation++;
        findChestAroundBlock(job.getWorkSpace());
        putSimInvInChest();
        if (!validateWorkArea()) {
            validateSentence = false;
            sim.getJob().setState(Activity.NOT_WORKING);
        } else if (validateWorkArea()) {
            sim.getJob().setState(Activity.WORKING);
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
        if (navigation  >= 20 * 8 && isEmptyChest()) {
            if (farmerWorkSpace.isEmpty()) {
                getSimFarmers();
                getFarmWorkSpace();
                getFarmChests();
            }
            navigation = 0;
        }
        if (!farmerWorkSpace.isEmpty()) {
            state = State.NAVIGATING;
            destinationBlock = farmerWorkSpace.get(0);
            sim.getNavigator().tryMoveToXYZ(destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ(), sim.getAIMoveSpeed()*2);
            if (sim.getPosition().withinDistance(new Vector3d(destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ()), 3)) {
                state = State.COLLECTING_WHEAT;
                for (BlockPos workChests : farmChestsHashMap.get(destinationBlock)) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(workChests);
                    if (chestTileEntity != null) {
                        int simInvLeftOver;
                        int farmChestCount = Math.min(chestTileEntity.count(Items.WHEAT), 192);
                        int simInvFarmStack = farmChestCount / 64;
                        if (simInvFarmStack >= 1) simInvLeftOver = farmChestCount - (simInvFarmStack * 64);
                        else {
                            simInvFarmStack = 0;
                            simInvLeftOver = farmChestCount;
                        }
                        for (int i = 0; i < chestTileEntity.getSizeInventory(); i++) {
                            ItemStack wheatFarmStack = chestTileEntity.getStackInSlot(i);
                            int wheatFarmAmount = wheatFarmStack.getCount();
                            if (wheatFarmStack.getItem().equals(Items.WHEAT)) {
                                if (farmChestCount >= wheatFarmAmount) {
                                    farmChestCount -= wheatFarmAmount;
                                    chestTileEntity.setInventorySlotContents(i, ItemStack.EMPTY);
                                } else {
                                    int chestLeftOver = wheatFarmAmount - farmChestCount;
                                    chestTileEntity.setInventorySlotContents(i, new ItemStack(Items.WHEAT, chestLeftOver));
                                    farmChestCount = 0;
                                }
                            }
                            if (farmChestCount == 0) break;
                        }
                        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++) {
                            ItemStack simInvStack = sim.getInventory().getStackInSlot(i);
                            if (simInvStack.isEmpty()) {
                                if (simInvFarmStack != 0) {
                                    simInvFarmStack--;
                                    sim.getInventory().setInventorySlotContents(i, new ItemStack(Items.WHEAT, 64));
                                } else {
                                    sim.getInventory().setInventorySlotContents(i, new ItemStack(Items.WHEAT, simInvLeftOver));
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
                    destinationBlock = sim.getJob().getWorkSpace();
                    farmerWorkSpace.clear();
                } else {
                    farmerWorkSpace.remove(0);
                    if (farmerWorkSpace.isEmpty()) {
                        destinationBlock = sim.getJob().getWorkSpace();
                        state = State.NAVIGATING_BACK;
                    }
                }
            }
        }
        if (farmerWorkSpace.isEmpty() && state.equals(State.NAVIGATING_BACK)) {
            sim.getNavigator().tryMoveToXYZ(destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ(), sim.getAIMoveSpeed()*2);
            if (sim.getPosition().withinDistance(new Vector3d(destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ()), 1)) {
                state = State.CHECKING_WHEAT;
            }
        }
    }

    @Override
    public double getTargetDistanceSq() {
        return 1.0d;
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

    @Override
    protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
        return sim.getDistanceSq(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()) > getTargetDistanceSq();
    }

    private void checkForWheat() {
        if (state == State.CHECKING_WHEAT) {
            for (BlockPos chest : chests) {
                ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(chest);
                if (chestTileEntity != null) {
                    wheatAmount += chestTileEntity.count(Items.WHEAT);
                }
            }
        }
    }

    private void produceBread() {
        int bread = wheatAmount / 3;
        int wheatToBeRemoved = bread * 3;
        chestLoop:
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(chest);
            if (chestTileEntity != null) {
                for (int i = chestTileEntity.getSizeInventory()-1; i >= 0; i--) {
                    ItemStack wheatStack = chestTileEntity.getStackInSlot(i);
                    int wheatCount = wheatStack.getCount();
                    if (wheatStack.getItem().equals(Items.WHEAT)) {
                        if (wheatToBeRemoved >= 64) {
                            wheatToBeRemoved -= wheatCount;
                            chestTileEntity.removeStackFromSlot(i);
                        } else if (wheatToBeRemoved == 0) { break; } else {
                            if (wheatToBeRemoved >= wheatCount) {
                                wheatToBeRemoved -= wheatCount;
                                chestTileEntity.removeStackFromSlot(i);
                            } else {
                                int wheatLeft = wheatCount - wheatToBeRemoved;
                                ItemStack newWheatStack = new ItemStack(Items.WHEAT, wheatLeft);
                                chestTileEntity.setInventorySlotContents(i, newWheatStack);
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < chestTileEntity.getSizeInventory(); i++)
                {
                    if (chestTileEntity.getStackInSlot(i).isEmpty()) {
                        chestTileEntity.setInventorySlotContents(i, new ItemStack(Items.BREAD, bread));
                        break chestLoop;
                    }
                }
            }

        }
    }

    private void getSimFarmers() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID());
        ArrayList<UUID> simFarmersUUID = faction.getEmployedSims();
        ServerWorld serverWorld = (ServerWorld) this.world;
        for (UUID simFarmerUUID : simFarmersUUID) {
            SimEntity simEntity = (SimEntity) serverWorld.getEntityByUuid(simFarmerUUID);
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
                TileFarmer tileFarmer = (TileFarmer) world.getTileEntity(jobFarmer.getWorkSpace());
                if (tileFarmer != null) {
                    if (tileFarmer.getSeed().equals(Seed.WHEAT) && !farmerWorkSpace.contains(jobFarmer.getWorkSpace()) && tileHasWheat(jobFarmer.getWorkSpace())) {
                        farmerWorkSpace.add(jobFarmer.getWorkSpace());
                    }
                }
            }
        }
    }

    private boolean tileHasWheat(BlockPos workSpace) {
        ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundPosition(workSpace, 5);
        for (BlockPos blockPos : blockPosArrayList) {
            if (world.getTileEntity(blockPos) instanceof ChestTileEntity) {
                ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(blockPos);
                if (chestTileEntity != null) {
                    if (chestTileEntity.count(Items.WHEAT) > 0) return true;
                }
            }
        }
        return false;
    }

    private void getFarmChests() {
        for (BlockPos farmBox : farmerWorkSpace) {
            ArrayList<BlockPos> chestArrayList = new ArrayList<>();
            ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundAndBelowPosition(farmBox, 5);
            for (BlockPos blockPos : blockPosArrayList) {
                if (world.getTileEntity(blockPos) instanceof ChestTileEntity) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(blockPos);
                    if (chestTileEntity != null) {
                        if (chestTileEntity.count(Items.WHEAT) > 0) {
                            chestArrayList.add(blockPos);
                        }
                    }
                }
            }
            farmChestsHashMap.put(farmBox, chestArrayList);
        }
    }

    private void findChestAroundBlock(BlockPos workPos) {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundAndBelowPosition(workPos, 5);
        for (BlockPos pos: blocks) {
            if (world.getTileEntity(pos) instanceof ChestTileEntity && !chests.contains(pos)) {
                chests.add(pos);
            }
        }
    }

    private boolean checkSimFull() {
        return sim.getInventory().count(Items.WHEAT) >= 192;
    }

    private void putSimInvInChest() {
        int simInvLeftOver;
        int simInvAmount = sim.getInventory().count(Items.WHEAT);
        int simInvStack = simInvAmount / 64;
        if (simInvStack >= 1) {
            simInvLeftOver = simInvAmount - (simInvAmount * 64);
        } else {
            simInvStack = 0;
            simInvLeftOver = simInvAmount;
        }
        ChestLoop:
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(chest);
            if (chestTileEntity != null) {
                for (int i = 0; i < chestTileEntity.getSizeInventory(); i++) {
                    ItemStack wheatStack = chestTileEntity.getStackInSlot(i);
                    if (wheatStack.isEmpty()) {
                        if (simInvStack != 0) {
                            simInvStack--;
                            chestTileEntity.setInventorySlotContents(i, new ItemStack(Items.WHEAT, 64));
                        } else {
                            chestTileEntity.setInventorySlotContents(i, new ItemStack(Items.WHEAT, simInvLeftOver));
                            break ChestLoop;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++) {
            ItemStack simWheatStack = sim.getInventory().getStackInSlot(i);
            if (simWheatStack.getItem().equals(Items.WHEAT)) sim.getInventory().setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    private boolean isEmptyChest() {
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(chest);
            if (chestTileEntity != null) {
                if (chestTileEntity.count(Items.BREAD) == 0) return true;
            }
        }
        return false;
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID());
        if (job.getWorkSpace() != null) {
            if (chests.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobBaker) has no inventory at: " + job.getWorkSpace(), world);
            } else {
                if (!validateSentence) faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobBaker) has started working", world);
                validateSentence = true;
                return true;
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