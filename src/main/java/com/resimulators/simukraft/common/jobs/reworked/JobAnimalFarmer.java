package com.resimulators.simukraft.common.jobs.reworked;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileAnimalFarm;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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

public class JobAnimalFarmer implements IReworkedJob {
    private final SimEntity sim;
    private int periodsWorked = 0;
    private BlockPos workSpace,blockPos;
    private Activity activity = Activity.NOT_WORKING;
    private boolean finished;

    private final int killsBeforeEmpty = 10;
    private final World world;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private final ArrayList<Integer> itemsToMove = new ArrayList<>();
    private final List<Item> drops = new ArrayList<>();
    private int currentKillCount;
    private int tick = 20;
    private TileAnimalFarm farm;
    private State state = State.MOVING;
    private AnimalEntity target;
    private LootTable table;

    public JobAnimalFarmer(SimEntity sim) {
        this.sim = sim;
        this.world = sim.level;

    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void setActivity(Activity state) {
        this.activity = state;
    }

    @Override
    public Profession jobType() {
        return Profession.ANIMAL_FARMER;
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
        return periodsWorked;
    }


    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT data = new CompoundNBT();
        data.putInt("id", sim.getProfession());
        nbt.add(data);
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsWorked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.asLong());
        }
        other.putBoolean("finished", finished);
        other.putInt("currentKillCount",currentKillCount);
        nbt.add(other);
        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsWorked = list.getInt("periodsworked");
            }
            if (list.contains("jobpos")) {
                setWorkSpace(BlockPos.of(list.getLong("jobpos")));
            }
            if (list.contains("finished")) {
                finished = list.getBoolean("finished");
            }
            if (list.contains("currentKillCount")){
                currentKillCount = list.getInt("currentKillCount");
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(++periodsWorked);
    }

    @Override
    public void setWorkedPeriods(int periods) {
        periodsWorked = periods;
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
        sim.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        chests = Utils.findInventoriesAroundPos(sim.getJob().getWorkSpace(), 4, world);
        if (farm == null) {
            farm = (TileAnimalFarm) world.getBlockEntity(getWorkSpace());
            ResourceLocation resourcelocation = farm.getAnimal().getAnimal().getDefaultLootTable();
            table = this.world.getServer().getLootTables().get(resourcelocation);

        }else{
            sim.setActivity(Activity.WORKING);
        }
    }

    @Override
    public void tick() {
        if (farm == null) {
            sim.setActivity(Activity.FORCE_STOP);
            return;
        }
        if (tick <= 0) {
            spawnNewAnimals();
            if (blockPos != null) {
                if (state == State.MOVING) {
                    sim.setStatus("Moving Towards target");
                    sim.getLookControl().setLookAt(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

                    if (sim.getNavigation().getPath() != null){
                        if (!sim.getNavigation().getPath().sameAs(sim.getNavigation().createPath(blockPos,(int)sim.getSpeed() * 2))){
                            getNewPath();
                        }else{
                            sim.getNavigation().moveTo( sim.getNavigation().getPath(), sim.getSpeed() * 2);
                        }
                    }else{
                        getNewPath();
                    }
                    System.out.println(sim.blockPosition().distSqr(blockPos));
                    if (sim.blockPosition().closerThan(blockPos, 2f)) {
                        state = State.ATTACKING;
                    }


                } else if (state == State.ATTACKING) {
                    sim.setStatus("Attacking Target");

                    if (target != null){
                        sim.getMainHandItem().getItem().hurtEnemy(sim.getMainHandItem(), target, sim);
                        sim.doHurtTarget(target);
                        sim.getLookControl().setLookAt(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                        sim.swing(sim.getUsedItemHand(),true);
                        if (target.isDeadOrDying()) {
                            target = null;
                            state = State.MOVING;

                            currentKillCount++;
                            System.out.println(currentKillCount);
                            if (currentKillCount >= killsBeforeEmpty) {
                                state = State.RETURNING;
                                LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getCommandSenderWorld()).withRandom(new Random())
                                        .withParameter(LootParameters.THIS_ENTITY, sim)
                                        .withParameter(LootParameters.ORIGIN, sim.position())
                                        .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.GENERIC);
                                LootContext ctx = builder.create(LootParameterSets.ENTITY);
                                table.getRandomItems(ctx).forEach(itemStack -> drops.add(itemStack.getItem()));

                                getCollectedItemStacks();
                                currentKillCount = 0;
                                sim.setStatus("Emptying inventory");
                            }else{
                                getNewTarget();
                            }

                        }
                    }else{
                        getNewTarget();
                        state = State.MOVING;
                    }

                } else if (state == State.RETURNING) {
                    addItemsToChests();
                    state = State.MOVING;
                    if (!(sim.getMainHandItem().getItem() instanceof SwordItem)) {
                        sim.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

                    }
                }
            } else {
                getNewTarget();
            }
            tick = 20;
        } else {
            tick -= 1;
        }
    }


    private void spawnNewAnimals() {
        if (!farm.hasMaxAnimals()) {
            farm.spawnAnimal();
        }

    }

    private void getCollectedItemStacks() {

        for (int i = 0; i < sim.getInventory().getContainerSize(); i++) {
            ItemStack stack = sim.getInventory().getItem(i);
            if (drops.contains(stack.getItem()) || stack.getItem().getTags().contains(ItemTags.WOOL.getName())) {
                itemsToMove.add(i);
            }

        }
        drops.clear();

    }

    private void getNewTarget(){
        AxisAlignedBB area = new AxisAlignedBB(getWorkSpace().offset(-4, 0, -4), getWorkSpace().offset(4, 2, 4));
        List<AnimalEntity> entities = world.getLoadedEntitiesOfClass(AnimalEntity.class, area);
        entities = entities
            .stream()
            .filter(animal -> animal.getType() == farm.getAnimal().getAnimal())
            .sorted(Comparator.comparingDouble(animalEntity -> getWorkSpace().distSqr(animalEntity.blockPosition())))
            .collect(Collectors.toList());


        if (entities.size() > 0) {
            blockPos = entities.get(0).blockPosition();
            target = entities.get(0);
        }
    }

    private void getNewPath(){
        sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
    }

    private boolean addItemsToChests() {
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(pos);
            if (chest == null) {
                chests.remove(pos);
                continue;
            }
            InvWrapper wrapper = new InvWrapper((chest));
            List<Integer> invStacks = new ArrayList<>(itemsToMove);
            for (int i = 0; i < invStacks.size(); i++) {
                if (ItemHandlerHelper.insertItemStacked(wrapper, sim.getInventory().getItem(invStacks.get(i)), false) == ItemStack.EMPTY)
                {
                    sim.getInventory().setItem(i, ItemStack.EMPTY);
                    itemsToMove.remove(invStacks.get(i));
                }
                if (itemsToMove.size() == 0) {
                    break; //all items have been added
                }
            }
        }
        if (itemsToMove.size() != 0){
            SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(String.format("%s %s has no more inventory space to empty collected items",farm.getName() +"er",sim.getCustomName().getString()),world);
            sim.setActivity(Activity.FORCE_STOP);
            sim.setStatus("Not working");
        }
        return false;
    }


    private enum State {
        MOVING,
        ATTACKING,
        RETURNING,


    }
}
