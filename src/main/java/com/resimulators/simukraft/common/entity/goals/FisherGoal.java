package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.old.JobFisher;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class FisherGoal extends BaseGoal<JobFisher> {

    private final SimEntity sim;
    private final World world;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final Item[] fish = {Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON, Items.COD};
    private final Random rnd = new Random();
    private State state = State.WAITING;
    private int tick, fishTrigger;
    private int delay = rnd.nextInt(41) + 5;
    private boolean validateSentence = false;


    //TODO: Fisher Mechanics (Swing) - CrAzyScreamX

    public FisherGoal(SimEntity sim) {
        super(sim, sim.getSpeed() * 2, 20);
        this.sim = sim;
        this.world = sim.level;
    }

    @Override
    public boolean canContinueToUse() {
        if (sim.getJob() != null) {
            if (sim.getJob().getActivity() == Activity.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            }
        }
        return canUse() && super.canContinueToUse();
    }

    @Override
    public boolean canUse() {
        job = (JobFisher) sim.getJob();
        if (job == null) return false;
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (sim.blockPosition().closerThan(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                sim.setActivity(Activity.WORKING);
                findChestsAroundTargetBlock(job.getWorkSpace(), 5, world);
                return true;
            }
        }

        return false;
    }

    @Override
    public void start() {
        sim.setItemInHand(sim.getUsedItemHand(), Items.FISHING_ROD.getDefaultInstance());
        if (!chests.isEmpty()) {
            state = State.CHEST_INTERACTION;
            blockPos = chests.get(0);
        } else {
            if (!validateWorkArea()) {
                sim.getJob().setActivity(Activity.NOT_WORKING);
            }
        }
    }

    @Override
    public double acceptedDistance() {
        return 1.0d;
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUUID());
        if (job.getWorkSpace() != null) {
            if (chests.isEmpty()) {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has no inventory at " + job.getWorkSpace(), world);
            } else if (findWater().equals(BlockPos.ZERO)) {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has no water at " + job.getWorkSpace(), world);
            } else {
                if (!validateSentence)
                    faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Fisherman) has started work at " + job.getWorkSpace(), world);
                validateSentence = true;
                return true;
            }
        }
        validateSentence = true;
        return false;
    }

    private BlockPos findWater() {
        final BlockPos[] water = {BlockPos.ZERO};
        Iterable<BlockPos> blockPoses = BlockPos.betweenClosedStream(job.getWorkSpace().offset(-5, -5, -5), job.getWorkSpace().offset(5, 0, 5))
                .filter(blockPos -> world.getFluidState(blockPos).getType().isSame(Fluids.WATER))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(blockPos -> job.getWorkSpace().distSqr(blockPos)))
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
        super.tick();
        tick++;
        fishTrigger++;
        state = State.WAITING;
        findChestsAroundTargetBlock(job.getWorkSpace(), 5, world);
        if (!validateWorkArea()) {
            sim.getJob().setActivity(Activity.NOT_WORKING);
            validateSentence = false;
        } else if (validateWorkArea()) {
            sim.getJob().setActivity(Activity.WORKING);
        }
        if (fishTrigger >= delay * 20 && sim.getActivity().equals(Activity.WORKING)) {
            state = State.FISHING;
            double f = rnd.nextDouble();
            int index = -1;
            if (f > .0 && f <= .02) { // Tropical Fish
                index = 0;
            } else if (f > .02 && f <= .15) { // PufferFish
                index = 1;
            } else if (f > .15 && f <= .40) { // Salmon
                index = 2;
            } else if (f > .40 && f <= 1.0) { // Cod
                index = 3;
            }
            state = State.CHEST_INTERACTION;
            for (BlockPos chest : chests) {
                ChestTileEntity chestEntity = (ChestTileEntity) world.getBlockEntity(chest);
                if (chestEntity != null) {
                    for (int i = 0; i < chestEntity.getContainerSize(); i++) {
                        ItemStack fishItem = chestEntity.getItem(i);
                        if (!fishItem.isEmpty()) {
                            if (fishItem.getItem().equals(fish[index])) {
                                if (fishItem.getCount() != 64) {
                                    fishItem.setCount(fishItem.getCount() + 1);
                                    chestEntity.setItem(i, fishItem);
                                    break;
                                }
                            }
                        } else {
                            chestEntity.setItem(i, new ItemStack(fish[index]));
                            break;
                        }
                    }
                }
                break;
            }
            delay = rnd.nextInt(41) + 5;
            fishTrigger = 0;
        }
    }

    @Override
    protected boolean isValidTarget(IWorldReader worldIn, BlockPos pos) {
        return sim.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > acceptedDistance();
    }

    private enum State {
        FISHING,
        CHEST_INTERACTION,
        WAITING
    }
}
