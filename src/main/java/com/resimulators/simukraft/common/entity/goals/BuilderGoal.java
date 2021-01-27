package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.jobs.JobBuilder;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModBlockProperties;
import com.resimulators.simukraft.init.ModBlocks;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.storage.WorldSavedData;

import java.util.List;

public class BuilderGoal extends MoveToBlockGoal {
    private final SimEntity sim;
    private int tick;
    private BuildingTemplate template;
    private List<Template.BlockInfo> blocks;
    private State state = State.STARTING;
    private int blockIndex = 0;
    private BlockPos origin;
    private int delay = 20;
    Rotation rotation;
    public BuilderGoal(SimEntity sim) {
        super(sim, .7d, 20);
        this.sim = sim;
    }

    @Override
    public boolean shouldExecute() {
        JobBuilder job = ((JobBuilder) sim.getJob());
        //System.out.println("startExecuting");
        if (job != null) {
            template = job.getTemplate();


            if (sim.getActivity() == Activity.GOING_TO_WORK) {
                if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                    sim.setActivity(Activity.WORKING);
                    return template != null;
                }
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        //done the condition checking for it starting just need the rest done
        //TODO: the Builder AI
        //FABBE50
        JobBuilder job = ((JobBuilder) sim.getJob());
        System.out.println("startExecuting");
        template = job.getTemplate();

        if (template != null) {

            Direction orgDir = template.getDirection();
            Direction facing = job.getDirection();
            rotation = getRotation(orgDir,facing);
            PlacementSettings settings = new PlacementSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE);
            blocks = StructureHandler.modifyAndConvertTemplate(template, sim.world, sim.getJob().getWorkSpace().offset(facing),settings);
            SimuKraft.LOGGER().debug("cost: " + template.getCost());

            /*for (Template.BlockInfo blockInfo : blocks) {
                BlockState state = blockInfo.state;
                if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BOX.get()) {
                    state = blockInfo.state.with(ModBlockProperties.TYPE, template.getTypeID());
                }
                if(blockInfo.state.getBlock() != Blocks.AIR) sim.world.setBlockState(blockInfo.pos, Blocks.COBBLESTONE.getDefaultState());
            }*/
        }
    }

    @Override
    public void tick() {
        tick++;
        super.tick();
        if (delay >= 0){
            delay = 60;
            if (state == State.STARTING){
                state = State.TRAVELING;
                destinationBlock = blocks.get(blockIndex).pos;
            }
            if (state == State.TRAVELING){
                if (sim.getDistanceSq(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()) < 20){
                    state = State.BUILDING;
                    return;
                }
            }
            if (state == State.BUILDING){
                Template.BlockInfo blockInfo = blocks.get(blockIndex);
                BlockState blockstate = blockInfo.state;
                if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BOX.get()) {
                    blockstate = blockInfo.state.with(ModBlockProperties.TYPE, template.getTypeID());
                }
                sim.world.setBlockState(blockInfo.pos, blockstate.rotate(sim.world,blockInfo.pos,rotation));

                blockIndex++;
                if (blockIndex < blocks.size() - 1) {
                destinationBlock = blocks.get(blockIndex).pos;
                sim.getNavigator().setPath(null,7d);
                state = State.TRAVELING;

                }
            }
        } else {
            delay--;
        }
        if (blockIndex >= blocks.size()){
            sim.getJob().setState(Activity.FORCE_STOP);
            Faction faction = SavedWorldData.get(sim.getEntityWorld()).getFactionWithSim(sim.getUniqueID());
            faction.sendFactionChatMessage("Builder " + sim.getName().getString() + "has finished building " + template.getName().replace("_" ," "), sim.getEntityWorld());
            if (template.getTypeID() == BuildingType.RESIDENTIAL.id)
            faction.addNewHouse(template.getControlBlock(),template.getName(),template.getRent());
            blockIndex = 0;

            template = null;

        }
    }

    @Override
    public boolean shouldMove() {
        return sim.getDistanceSq(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()) > getTargetDistanceSq();
    }

    @Override
    protected boolean shouldMoveTo(IWorldReader iWorldReader, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (sim.getJob() != null){
        if (sim.getJob().getState() == Activity.FORCE_STOP) {
            return false;
        }
        if (tick < sim.getJob().workTime()) {
            return true;
        } else {
            sim.getJob().finishedWorkPeriod();
            sim.getJob().setState(Activity.NOT_WORKING);
        }}

        return false;
    }

    private Rotation getRotation(Direction org, Direction cur) {
        if (org == cur.rotateY()){
            return Rotation.COUNTERCLOCKWISE_90;
        }
        if (org == cur.rotateYCCW()){
            return Rotation.CLOCKWISE_90;
        }
        if (org == cur.getOpposite()){
            return Rotation.CLOCKWISE_180;
        }

        return Rotation.NONE;
    }

    //Checks if Sim has the building materials in inventory.
    private boolean hasItems() {
        //Returns true until logic has been implemented.
        return true;
    }

    //Checks for inventories around position.
    private void checkForInventories(BlockPos pos) {

    }

    //Scans inventories and makes Sim go get items from chest that contains it.
    private void retrieveItemsFromChest(BlockPos pos) {

    }

    private enum State {
        STARTING,
        TRAVELING,
        BUILDING,
        WAITING
    }
}
