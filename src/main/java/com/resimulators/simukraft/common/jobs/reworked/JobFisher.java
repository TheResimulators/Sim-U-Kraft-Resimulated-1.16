package com.resimulators.simukraft.common.jobs.reworked;

import com.mojang.authlib.GameProfile;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class JobFisher implements IReworkedJob {


    private final SimEntity sim;
    private int periodsworked = 0;
    private BlockPos workSpace, blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private boolean finished;


    private final World world;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private final Item[] fish = {Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON, Items.COD};
    private final Random rnd = new Random();
    private State state = State.WAITING;
    private int tick, fishTrigger;

    private int fishCollectedCount = 0;
    private int delay = rnd.nextInt(41) + 5;
    private boolean validateSentence = false;
    private int index = -1;

    private int waitingPeriod = 100;

    private ArrayList<Integer> fishCaughtList;
    private FakePlayer player;

    public JobFisher(SimEntity simEntity) {
        this.sim = simEntity;
        this.world = sim.level;
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
        return Profession.FISHER_MAN;
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
        sim.setStatus("Stopped Working");
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




        sim.setItemInHand(sim.getUsedItemHand(), Items.FISHING_ROD.getDefaultInstance());

        if (player == null && !sim.level.isClientSide) {
            player = new FakePlayer((ServerWorld) sim.level, new GameProfile(null, "FisherMan_" + sim.getUUID()));
        }



        if (!chests.isEmpty()) {
            sim.setActivity(Activity.WORKING);
            state = State.FISHING;
            if (fishCaughtList != null) fishCaughtList.clear();
            else {
                fishCaughtList = new ArrayList<>();
            }
            blockPos = chests.get(0);
        } else {
            if (!validateWorkArea()) {
                sim.getJob().setActivity(Activity.NOT_WORKING);
            }
        }
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        if (getWorkSpace() != null) {
            chests = Utils.findInventoriesAroundPos(getWorkSpace(), 5, world);
            if (chests.isEmpty()) {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has no inventory at " + getWorkSpace(), world);
            } else if (findWater().equals(BlockPos.ZERO)) {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has no water at " + getWorkSpace(), world);
            } else {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has started work at " + getWorkSpace(), world);
                validateSentence = true;
                return true;
            }
        }
        validateSentence = true;
        return false;
    }

    private BlockPos findWater() {
        final BlockPos[] water = {BlockPos.ZERO};
        Iterable<BlockPos> blockPoses = BlockPos.betweenClosedStream(getWorkSpace().offset(-5, -5, -5), getWorkSpace().offset(5, 0, 5))
                .filter(blockPos -> world.getFluidState(blockPos).getType().isSame(Fluids.WATER))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(blockPos -> getWorkSpace().distSqr(blockPos)))
                .collect(Collectors.toCollection(ArrayList::new));
        for (BlockPos blockPos : blockPoses) {
            if (world.getFluidState(blockPos).getType().isSame(Fluids.WATER)) {
                water[0] = blockPos;
                break;

            }
        }

        return water[0];
    }

    @Override
    public void tick() {
        tick++;
        fishTrigger++;


        chests = Utils.findInventoriesAroundPos(getWorkSpace(), 5, world);
        if (!validateWorkArea()) {
            setActivity(Activity.NOT_WORKING);
            validateSentence = false;
        } else if (validateWorkArea()) {
            setActivity(Activity.WORKING);
        }

        if (sim.blockPosition().distManhattan(sim.getJob().getWorkSpace()) >= 2)
        {
            state = State.MOVING;
        }

        if (state == State.MOVING)
        {
           Path path =  sim.getNavigation().createPath(sim.getJob().getWorkSpace(),(int)sim.getSpeed());
           sim.getNavigation().moveTo(path,sim.getSpeed());
            if(sim.blockPosition().distManhattan(sim.getJob().getWorkSpace()) < 2 )
            {
                state = State.FISHING;
            }
        }
        if (state == State.FISHING) {

            if (fishTrigger >= delay * 20) {

                sim.setStatus("Fishing");
                sim.swing(Hand.MAIN_HAND,true);
                sim.getMainHandItem().getItem().use(sim.getCommandSenderWorld(),player,Hand.MAIN_HAND);
                double f = rnd.nextDouble();

                if (f <= .02) { // Tropical Fish
                    index = 0;
                } else if (f <= .10) { // PufferFish
                    index = 1;
                } else if (f <= .30) { // Salmon
                    index = 2;
                } else if (f <= .70) { // Cod
                    index = 3;
                } else {
                    index = -1;
                    // did not find a fish
                }
                if (index != -1) {

                    fishTrigger = 0;
                    fishCollectedCount++;
                    delay = rnd.nextInt(2) + 5;
                    fishCaughtList.add(index);
                    if (fishCollectedCount >= 8) {
                        state = State.CHEST_INTERACTION;

                    }
                }
            } else {
              fishTrigger++;
            }

        }
        if (state == State.CHEST_INTERACTION) {
            sim.setStatus("Putting fish away");
            boolean success = false;
            for (int fishIndex = 0; fishIndex < fishCaughtList.size() ; fishIndex++) {
                for (BlockPos chest : chests) {
                    ChestTileEntity chestEntity = (ChestTileEntity) world.getBlockEntity(chest);
                    InvWrapper wrapper = new InvWrapper(chestEntity);
                    if (chestEntity != null) {
                        if (ItemHandlerHelper.insertItemStacked(wrapper, new ItemStack(fish[fishCaughtList.get(fishIndex)]), false) == ItemStack.EMPTY) {
                            state = State.WAITING;
                            success = true;
                            break;
                        }
                    }
                }

                if(!success)
                {
                    SavedWorldData.get(sim.getCommandSenderWorld()).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(
                            "Sim " + sim.getCustomName() + "Has ran out of space for its fish at " + sim.getJob().getWorkSpace().toString(),sim.getCommandSenderWorld());
                    finishedWorkPeriod();
                    sim.setActivity(Activity.NOT_WORKING);
                    break;

                }

            }
            fishCaughtList.clear();
        }

        if (state == State.WAITING) {

            if (tick >= waitingPeriod) {
                state = State.FISHING;
                tick = 0;
                fishCollectedCount = 0;
            }else {

                sim.setStatus("Waiting to Fish");
                tick++;
            }
        }
    }


    private enum State {
        FISHING,
        CHEST_INTERACTION,
        WAITING,

        MOVING
    }
}


