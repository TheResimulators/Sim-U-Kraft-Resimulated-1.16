package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobBaker;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BakerGoal extends BaseGoal<JobBaker> {

    private final SimEntity sim;
    private final World world;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private State state = State.WAITING;
    private int tick, delay = 0, wheatAmount;
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
        if (!chests.isEmpty()) {
            state = State.CHEST_INTERACTION;
            destinationBlock = chests.get(0);
        } else if (!validateWorkArea()) {
            sim.getJob().setState(Activity.NOT_WORKING);
        }
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
        delay++;
        findChestAroundBlock(job.getWorkSpace());
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

    private void findChestAroundBlock(BlockPos workPos) {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundPosition(workPos, 5);
        for (BlockPos pos: blocks) {
            if (world.getTileEntity(pos) instanceof ChestTileEntity && !chests.contains(pos)) {
                chests.add(pos);
            }
        }
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
        CHEST_INTERACTION
    }



}

