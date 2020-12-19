package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobBuilder;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModBlockProperties;
import com.resimulators.simukraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import java.util.List;

public class BuilderGoal extends Goal {
    private final SimEntity sim;
    private int tick;
    private BuildingTemplate template;
    private List<Template.BlockInfo> blocks;
    private BlockPos origin;

    public BuilderGoal(SimEntity sim) {
        this.sim = sim;
    }

    @Override
    public boolean shouldExecute() {
        System.out.println("shouldExecute");

        return true;

       /*
        IJob job = sim.getJob();
        if (sim.getActivity() == EnumJobState.GOING_TO_WORK) {
            if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                sim.setActivity(EnumJobState.WORKING);
                return true;
            }
        }

        return false;*/
    }


    @Override
    public void startExecuting() {
        //done the condition checking for it starting just need the rest done
        //TODO: the Builder AI
        //FABBE50
        System.out.println("startExecuting");
        template = ((JobBuilder) sim.getJob()).getTemplate();
        PlacementSettings settings = new PlacementSettings()
                .setCenterOffset(template.getSize().subtract(new Vector3i(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2)))
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE);
        blocks = StructureHandler.modifyAndConvertTemplate(template, sim.world, sim.getJob().getWorkSpace(), Rotation.NONE, Mirror.NONE); // needs to be fixed, only places packed ice right now
        SimuKraft.LOGGER().debug("cost: " + template.getCost());
        for (Template.BlockInfo blockInfo : blocks) {
            BlockState state = blockInfo.state;
            if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BOX.get()) {
                state = blockInfo.state.with(ModBlockProperties.TYPE, template.getTypeID());
            }
            sim.world.setBlockState(blockInfo.pos, state);
        }
    }

    @Override
    public void tick() {
        tick++;
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
}
