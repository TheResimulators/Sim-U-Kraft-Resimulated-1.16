package com.resimulators.simukraft.common.jobs.reworked;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileAnimalFarm;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JobButcher implements IReworkedJob {
    private final SimEntity sim;
    private final World world;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private final ArrayList<SimEntity> simAnimalFarmers = new ArrayList<>();
    private final ArrayList<BlockPos> animalFarmsWorkSpace = new ArrayList<>();
    private final HashMap<BlockPos, ArrayList<BlockPos>> farmChestsHashMap = new HashMap<>();
    private int periodsworked = 0;
    private BlockPos workSpace, blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private boolean finished = false;
    private State state = State.WAITING;
    private int delay = 0;
    private int targetIndex;
    private boolean validateSentence = false;

    public JobButcher(SimEntity simEntity) {
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
        return Profession.BUTCHER;
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
        chests = Utils.findInventoriesAroundPos(getWorkSpace(),10,world);
        if (!validateWorkArea()) {
            sim.getJob().setActivity(Activity.NOT_WORKING);
            return;
        }
        sim.setActivity(Activity.WORKING);
        state = State.START;

        }


    @Override
    public void tick() {
        delay++;

        if (delay >= 20 * 5) { // For now it's 100 (20*5) to for testing purposes, will be changed later on

            if (!validateWorkArea()) {
                validateSentence = false;
                sim.getJob().setActivity(Activity.NOT_WORKING);
            }

                if (state == State.START)
                {
                    if ( checkChestWorkplaceForRawMeat() > 0)
                    {
                        state = State.CHECKING_FOR_MEAT;
                    }else {
                        if (!animalFarmsWorkSpace.isEmpty())
                        {
                            targetIndex= 0;
                            blockPos = animalFarmsWorkSpace.get(targetIndex);

                            if (!sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 3))
                            {
                                state = State.NAVIGATING;
                                sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
                            }else
                            {
                                state = State.COLLECTING_RAW_MEAT;
                            }
                        }
                    }
                }
            }


            if (state == State.NAVIGATING)
            {
                if (sim.blockPosition().closerThan(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 3))
                {
                    state = State.COLLECTING_RAW_MEAT;
                }

            }

            if (state == State.COLLECTING_RAW_MEAT)
            {
                boolean full = false;
                for (BlockPos workChests : farmChestsHashMap.get(blockPos)) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(workChests);
                    collectMeatFromFarm(chestTileEntity);
                    if (checkSimFull()) {
                        state = State.NAVIGATING_BACK;
                        blockPos = sim.getJob().getWorkSpace();
                        animalFarmsWorkSpace.clear();
                        full = true;
                        break;
                    }
                }
                if (!full){
                    targetIndex++;
                    if (targetIndex > animalFarmsWorkSpace.size()) {
                        blockPos = sim.getJob().getWorkSpace();
                        state = State.NAVIGATING_BACK;
                    }else
                    {
                        state = State.NAVIGATING;
                        blockPos = animalFarmsWorkSpace.get(targetIndex);
                    }
                }
            }


    }


    private void collectMeatFromFarm(ChestTileEntity farmChest)
    {
        if (farmChest != null) {
            InvWrapper wrapper = new InvWrapper(farmChest);

            for (int i = 0; i < farmChest.getContainerSize(); i++) {
                if (sim.addItemStackToInventory(wrapper.getStackInSlot(i)))
                {
                    wrapper.setStackInSlot(i,ItemStack.EMPTY);
                }
            }

        }
    }

    private void putSimInvInChest() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        for (BlockPos chestPos: chests)
        {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(chestPos);
            InvWrapper wrapper = new InvWrapper((chest));
            for (int i = 0; i < sim.getInventory().mainInventory.size(); i++) {
                ItemStack stack = sim.getInventory().mainInventory.get(i);
                if (!stack.equals(ItemStack.EMPTY) && !(stack.getItem() instanceof ToolItem) && MEATS.containsRawItem(stack.getItem())) {
                    if (ItemHandlerHelper.insertItemStacked(wrapper,stack,false) != ItemStack.EMPTY){
                        SimuKraft.LOGGER().debug("No Room in chest");
                    }
                }
            }
            break;
        }
        if (countRawMeatItems(sim.getInventory()) > 0)
            faction.sendFactionChatMessage("Butcher " + sim.getName() + ", has ran out of inventory space",world);
            }


    private void getSimAnimalFarmers() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        ArrayList<UUID> simAnimalFarmersUUID = faction.getEmployedSims();
        ServerWorld serverWorld = (ServerWorld) this.world;
        for (UUID simFarmerUUID : simAnimalFarmersUUID) {
            SimEntity simEntity = (SimEntity) serverWorld.getEntity(simFarmerUUID);
            if (simEntity != null) {
                if (simEntity.getJob().jobType().equals(Profession.FARMER) && !simAnimalFarmers.contains(simEntity)) {
                    simAnimalFarmers.add(simEntity);
                }
            }
        }
    }

    private void getFarmWorkSpace() {
        for (SimEntity simFarmer : simAnimalFarmers) {
            JobAnimalFarmer jobFarmer = (JobAnimalFarmer) simFarmer.getJob();
            if (jobFarmer != null) {
                TileAnimalFarm tileFarmer = (TileAnimalFarm) world.getBlockEntity(jobFarmer.getWorkSpace());
                if (tileFarmer != null) {
                    if (!animalFarmsWorkSpace.contains(jobFarmer.getWorkSpace()) && tileHasMeat(jobFarmer.getWorkSpace())) {
                        animalFarmsWorkSpace.add(jobFarmer.getWorkSpace());
                    }
                }
            }
        }
    }

    private boolean tileHasMeat(BlockPos workSpace) {
        ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundPosition(workSpace, 5);
        for (BlockPos blockPos : blockPosArrayList) {
            if (world.getBlockEntity(blockPos) instanceof ChestTileEntity) {
                ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(blockPos);
                if (chestTileEntity != null) {
                    if (countRawMeatItems(chestTileEntity) > 0) return true;
                }
            }
        }
        return false;
    }
    private void getFarmChests() {
        for (BlockPos farmBox : animalFarmsWorkSpace) {
            ArrayList<BlockPos> chestArrayList = new ArrayList<>();
            ArrayList<BlockPos> blockPosArrayList = BlockUtils.getBlocksAroundAndBelowPosition(farmBox, 5);
            for (BlockPos blockPos : blockPosArrayList) {
                if (world.getBlockEntity(blockPos) instanceof ChestTileEntity) {
                    ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(blockPos);
                    if (chestTileEntity != null) {
                        if (countRawMeatItems(chestTileEntity) > 0) {
                            chestArrayList.add(blockPos);
                        }
                    }
                }
            }
            farmChestsHashMap.put(farmBox, chestArrayList);
        }
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        if (getWorkSpace() != null) {
            if (chests.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobButcher) has no inventory at: " + getWorkSpace(), world);
            } else {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (JobButcher) has started working", world);
                validateSentence = true;
                return true;
            }
        }
        return false;
    }

    private int checkChestWorkplaceForRawMeat() {
        int meatAmount = 0;

        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(chest);
            if (chestTileEntity != null) {
                meatAmount += countRawMeatItems(chestTileEntity);
            }
        }
        return meatAmount;
    }

    private boolean checkSimFull() {
        return countRawMeatItems(sim.getInventory()) >= 192;
    }

    private boolean isEmptyChest() {
        for (BlockPos chest : chests) {
            ChestTileEntity chestTileEntity = (ChestTileEntity) world.getBlockEntity(chest);
            if (chestTileEntity != null) {
                if (countAllMeatItems(chestTileEntity) == 0) return true;
            }
        }
        return false;
    }


    private int countAllMeatItems(IInventory entity)
    {
        int count = 0;
        for (MEATS meat: MEATS.values())
        {
            count += entity.countItem(meat.RawItem);
            count += entity.countItem(meat.CookedItem);
        }
        return count;
    }

    private int countRawMeatItems(IInventory entity)
    {
        int count = 0;
        for (MEATS meat: MEATS.values())
        {
            count += entity.countItem(meat.RawItem);
        }
        return count;
    }


    private int countCookedMeatItems(IInventory entity)
    {
        int count = 0;
        for (MEATS meat: MEATS.values())
        {
            count += entity.countItem(meat.CookedItem);
        }
        return count;
    }

    private enum State {
        START,
        WAITING,
        CHECKING_FOR_MEAT,
        COLLECTING_RAW_MEAT,
        PRODUCING_MEAT,
        CHEST_INTERACTION,
        NAVIGATING,
        NAVIGATING_BACK
    }

    private enum MEATS
    {
        BEEF(Items.BEEF,Items.COOKED_BEEF),
        CHICKEN(Items.CHICKEN, Items.COOKED_CHICKEN),
        MUTTON(Items.MUTTON, Items.COOKED_MUTTON),
        RABBIT(Items.RABBIT, Items.COOKED_RABBIT),
        PORK(Items.PORKCHOP, Items.COOKED_PORKCHOP),
        SALMON(Items.SALMON, Items.COOKED_SALMON),
        COD(Items.COD, Items.COOKED_COD);

        public Item RawItem;
        public Item CookedItem;
        MEATS(Item RawItem,Item CookedItem) {
            this.RawItem = RawItem;
            this.CookedItem = CookedItem;
        }


        public static boolean containsRawItem(Item stack){
            for (MEATS meat:  MEATS.values()){
                if (meat.RawItem ==stack) return true;
            }
            return false;
        }

        public static Item getCookedRawItem(Item stack){
            for (MEATS meat:  MEATS.values()){
                if (meat.RawItem ==stack) return meat.CookedItem;
            }
            return null;
        }
    }
}

