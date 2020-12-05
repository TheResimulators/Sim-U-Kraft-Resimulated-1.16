package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobFisher;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class FisherGoal extends BaseGoal<JobFisher> {

    private final SimEntity sim;
    private final World world;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private State state = State.WAITING;
    private final Item[] fish = {Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON, Items.COD};
    private int tick;
    private final Random rnd = new Random();
    private int delay = rnd.nextInt(41) + 5;


    public FisherGoal(SimEntity sim) {
        super(sim, sim.getAIMoveSpeed()* 2, 20);
        this.sim = sim;
        this.world = sim.world;
    }

    @Override
    public boolean shouldExecute() {
        job = (JobFisher) sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK) {
            if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                job.setState(EnumJobState.WORKING);
                findChestAroundBlock(job.getWorkSpace());
                findWaterBlocks(job.getWorkSpace());
                return validateWorkArea();
            }
        }

        return false;
    }

    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(),Items.FISHING_ROD.getDefaultInstance());
        if (!chests.isEmpty()) {
            state = State.CHEST_INTERACTION;
            destinationBlock = chests.get(0);
        }
        else {
            if (!validateWorkArea()) {
                sim.getJob().setState(EnumJobState.NOT_WORKING);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
        state = State.WAITING;

        if (tick >= delay * 20) {
            state = State.FISHING;
            double f = rnd.nextDouble();
            int index = -1;
            if (f > .0 && f <= .02) { // Tropical Fish
                index = 0;
            }
            else if (f > .02 && f <= .15) { // PufferFish
                index = 1;
            }
            else if (f > .15 && f <= .40) { // Salmon
                index = 2;
            }
            else if (f > .40 && f <= 1.0) { // Cod
                index = 3;
            }
            state = State.CHEST_INTERACTION;
            for (BlockPos chest: chests) {
                ChestTileEntity chestEntity = (ChestTileEntity) world.getTileEntity(chest);
                if (chestEntity != null) {
                    for (int i = 0; i < chestEntity.getSizeInventory(); i++) {
                        ItemStack fishItem = chestEntity.getStackInSlot(i);
                        if (!fishItem.isEmpty()) {
                            if (fishItem.getItem().equals(fish[index])) {
                                if (fishItem.getCount() != 64) {
                                    fishItem.setCount(fishItem.getCount()+1);
                                    chestEntity.setInventorySlotContents(i, fishItem);
                                    break;
                                }
                            }
                        }
                        else {
                            chestEntity.setInventorySlotContents(i, new ItemStack(fish[index]));
                            break;
                        }
                    }
                }
                break;
            }
            delay = rnd.nextInt(41) + 5;
            tick = 0;
        }
    }

    @Override
    public double getTargetDistanceSq() {
        return 1.0d;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (sim.getJob() != null) {
            if (sim.getJob().getState() == EnumJobState.FORCE_STOP) {
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

    private void findChestAroundBlock(BlockPos workPos) {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundPosition(workPos, 5);
        for (BlockPos pos: blocks) {
            if (world.getTileEntity(pos) instanceof ChestTileEntity) {
                chests.add(pos);
            }
        }
        
    }

    private void findWaterBlocks(BlockPos workPos) {
        //TODO: water blocks mechanic
    }

    private boolean validateFishingRod() {
        for (BlockPos chest : chests) {
            ChestTileEntity chestEntity = (ChestTileEntity) world.getTileEntity(chest);
            if (chestEntity != null) {
                for (int i = 0; i < chestEntity.getSizeInventory(); i++) {
                    ItemStack chestContains = chestEntity.getStackInSlot(i);
                    if (chestContains.getItem() instanceof FishingRodItem) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean validateWorkArea() {
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID());
        if (job.getWorkSpace() != null) {
            if (chests.isEmpty()) {
                faction.sendFactionChatMessage(sim.getDisplayName() + " (Fisherman) has no inventory at " + job.getWorkSpace(), world);
            }
            //else if (water.isEmpty()) {
                //faction.sendFactionChatMessage(sim.getDisplayName() + " (Fisherman) has no water at " + job.getWorkSpace(), world);
            //}
            else if (!validateFishingRod()) {
                faction.sendFactionChatMessage(sim.getDisplayName() + " (Fisherman) has no fishing rod at: " + job.getWorkSpace() + ", using normal fishing rod", world);
                return true;
            }
            else {
                faction.sendFactionChatMessage(sim.getDisplayName() + " (Fisherman) has started work at " + job.getWorkSpace(), world);
                return true;
            }
        }
        return false;
    }

    private enum State {
        FISHING,
        CHEST_INTERACTION,
        WAITING
    }
}
