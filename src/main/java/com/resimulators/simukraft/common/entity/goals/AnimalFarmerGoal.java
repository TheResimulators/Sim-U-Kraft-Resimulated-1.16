package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobAnimalFarmer;
import com.resimulators.simukraft.common.jobs.JobGlassFactory;
import com.resimulators.simukraft.common.jobs.core.EnumJobState;
import com.resimulators.simukraft.common.tileentity.TileAnimalFarm;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AnimalFarmerGoal extends BaseGoal<JobAnimalFarmer>{
    private final SimEntity sim;
    private int currentKillCount;
    private final int killsBeforeEmpty = 10;
    private final World world;
    private int tick = 20;
    private TileAnimalFarm farm;
    private State state = State.MOVING;
    private AnimalEntity target;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    public AnimalFarmerGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
        this.sim = sim;
        this.world = sim.world;
    }


    @Override
    public boolean shouldExecute() {
        job = (JobAnimalFarmer) sim.getJob();
        if (job.getState() == EnumJobState.GOING_TO_WORK){
            if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)) {
                job.setState(EnumJobState.WORKING);
                return true;
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        super.startExecuting();
        sim.setHeldItem(Hand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));
        findChestAroundBlock(sim.getJob().getWorkSpace());
    }

    @Override
    public void tick() {
        super.tick();
        if (farm == null){
            farm = (TileAnimalFarm) world.getTileEntity(job.getWorkSpace());
        }
        AxisAlignedBB area = new AxisAlignedBB(job.getWorkSpace().add(-4,0,-4),job.getWorkSpace().add(4,2,4));
        List<AnimalEntity> entities = world.getLoadedEntitiesWithinAABB(AnimalEntity.class,area);
        entities = entities
                .stream()
                .filter(animal -> animal.getType() == farm.getAnimal().getAnimal())
                .sorted(Comparator.comparingDouble(animalEntity ->job.getWorkSpace().distanceSq(animalEntity.getPosition())))
                .collect(Collectors.toList());

        if (tick <= 0){
            spawnNewAnimals();
            if (state == State.MOVING){
                if (entities.size() > 0){
                destinationBlock = entities.get(0).getPosition();
                target = entities.get(0);
                sim.lookAt(EntityAnchorArgument.Type.EYES,new Vector3d(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()));}
                System.out.println(sim.getPosition().distanceSq(destinationBlock));
                if (sim.getPosition().withinDistance(destinationBlock,6f)){
                    state = State.ATTACKING;
                    }

            }

            else if( state == State.ATTACKING){
                sim.swing(sim.getActiveHand(),true);
                sim.getHeldItemMainhand().getItem().hitEntity(sim.getHeldItemMainhand(),target,sim);
                sim.attackEntityAsMob(target);
                if (target.getShouldBeDead()){
                    target = null;
                    state = State.MOVING;

                    currentKillCount++;
                    if (currentKillCount >= killsBeforeEmpty){
                        state = State.RETURNING;
                    }

                }

            }
            else if (state == State.RETURNING){



            }


            tick = 20;
        }else {
            tick -= 1;
        }
    }



    private void spawnNewAnimals(){
        if (!farm.hasMaxAnimals()){
            farm.spawnAnimal();
        }

    }

    public void findChestAroundBlock(BlockPos workPos){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
    }

    private boolean addItemsToChests(){
        for (BlockPos pos: chests){
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            for (int i = 0; i< chest.getSizeInventory(); i++){


            }


        }

        return false;
    }

    private enum State {
        MOVING,
        ATTACKING,
        RETURNING,


    }
}
