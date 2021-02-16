package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobAnimalFarmer;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileAnimalFarm;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
    private ArrayList<Integer> itemsToMove = new ArrayList<>();
    private LootTable table;
    private List<Item> drops = new ArrayList<>();
    public AnimalFarmerGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
        this.sim = sim;
        this.world = sim.world;

    }


    @Override
    public boolean shouldExecute() {
        job = (JobAnimalFarmer) sim.getJob();
        if (job == null) return  false;
        if (sim.getActivity() == Activity.GOING_TO_WORK){
            if (job.getWorkSpace() != null){
                if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)) {
                    sim.setActivity(Activity.WORKING);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        super.startExecuting();
        sim.setHeldItem(Hand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));
        findChestAroundBlock(sim.getJob().getWorkSpace());
        if (farm == null){
            farm = (TileAnimalFarm) world.getTileEntity(job.getWorkSpace());
            ResourceLocation resourcelocation = farm.getAnimal().getAnimal().getLootTable();
            table = this.world.getServer().getLootTableManager().getLootTableFromLocation(resourcelocation);
        }
    }

    @Override
    public void tick() {
        super.tick();

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
                sim.setStatus("Moving Towards target");
                if (entities.size() > 0){
                destinationBlock = entities.get(0).getPosition();
                target = entities.get(0);
                sim.getLookController().setLookPosition(new Vector3d(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()));}
                System.out.println(sim.getPosition().distanceSq(destinationBlock));
                if (sim.getPosition().withinDistance(destinationBlock,6f)){
                    state = State.ATTACKING;
                    }

            }

            else if( state == State.ATTACKING){
                sim.setStatus("Attacking Target");
                sim.swingArm(sim.getActiveHand());
                sim.getHeldItemMainhand().getItem().hitEntity(sim.getHeldItemMainhand(),target,sim);
                sim.attackEntityAsMob(target);
                if (target.getShouldBeDead()){
                    target = null;
                    state = State.MOVING;

                    currentKillCount++;
                    if (currentKillCount >= killsBeforeEmpty){
                        state = State.RETURNING;
                        LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getEntityWorld()).withRandom(new Random()).withParameter(LootParameters.THIS_ENTITY, sim).withParameter(LootParameters.field_237457_g_, sim.getPositionVec()).withParameter(LootParameters.DAMAGE_SOURCE,DamageSource.GENERIC);
                        LootContext ctx = builder.build(LootParameterSets.ENTITY);
                        table.generate(ctx).forEach(itemStack -> drops.add(itemStack.getItem()));
                        getCollectedItemStacks();
                        currentKillCount = 0;
                        sim.setStatus("Emptying inventory");
                    }

                }

            }
            else if (state == State.RETURNING){

                addItemsToChests();
                state = State.MOVING;
                if (!(sim.getHeldItemMainhand().getItem() instanceof SwordItem)){
                    sim.setHeldItem(Hand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));

                }
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
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundAndBelowPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
    }

    private boolean addItemsToChests(){
        for (BlockPos pos: chests){
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            InvWrapper wrapper = new InvWrapper((chest));
            List<Integer> invStacks = new ArrayList<Integer>();
            invStacks.addAll(itemsToMove);
            for (int i = 0; i< invStacks.size(); i++){
                if (ItemHandlerHelper.insertItemStacked(wrapper,sim.getInventory().getStackInSlot(invStacks.get(i)),false) == ItemStack.EMPTY);{
                    sim.getInventory().setInventorySlotContents(i,ItemStack.EMPTY);
                    itemsToMove.remove(invStacks.get(i));
                }
                if (itemsToMove.size() == 0){
                    break; //all items have been added
                }
            }
        }

        return false;
    }


    private void getCollectedItemStacks(){

        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++){
            ItemStack stack = sim.getInventory().getStackInSlot(i);
            if (drops.contains(stack.getItem()) || stack.getItem().getTags().contains(ItemTags.WOOL.getName())){
                itemsToMove.add(i);
            }

        }
        drops.clear();

    }

    private enum State {
        MOVING,
        ATTACKING,
        RETURNING,


    }


    @Override
    public void resetTask() {
        super.resetTask();
        sim.setStatus("Idle");
    }
}
