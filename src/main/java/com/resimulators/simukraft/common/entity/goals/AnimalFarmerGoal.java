package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.old.JobAnimalFarmer;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileAnimalFarm;
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

public class AnimalFarmerGoal extends BaseGoal<JobAnimalFarmer> {
    private final SimEntity sim;
    private final int killsBeforeEmpty = 10;
    private final World world;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final ArrayList<Integer> itemsToMove = new ArrayList<>();
    private final List<Item> drops = new ArrayList<>();
    private int currentKillCount;
    private int tick = 20;
    private TileAnimalFarm farm;
    private State state = State.MOVING;
    private AnimalEntity target;
    private LootTable table;

    public AnimalFarmerGoal(SimEntity sim) {
        super(sim, sim.getSpeed() * 2, 20);
        this.sim = sim;
        this.world = sim.level;

    }


    @Override
    public boolean canUse() {
        job = (JobAnimalFarmer) sim.getJob();
        if (job == null) return false;
        if (sim.getActivity() == Activity.GOING_TO_WORK) {
            if (job.getWorkSpace() != null) {
                if (sim.blockPosition().closerThan(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                    sim.setActivity(Activity.WORKING);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void start() {
        super.start();
        sim.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        findChestsAroundTargetBlock(sim.getJob().getWorkSpace(), 5, world);
        if (farm == null) {
            farm = (TileAnimalFarm) world.getBlockEntity(job.getWorkSpace());
            ResourceLocation resourcelocation = farm.getAnimal().getAnimal().getDefaultLootTable();
            table = this.world.getServer().getLootTables().get(resourcelocation);
        }
    }

    @Override
    public void tick() {
        super.tick();

        AxisAlignedBB area = new AxisAlignedBB(job.getWorkSpace().offset(-4, 0, -4), job.getWorkSpace().offset(4, 2, 4));
        List<AnimalEntity> entities = world.getLoadedEntitiesOfClass(AnimalEntity.class, area);
        entities = entities
                .stream()
                .filter(animal -> animal.getType() == farm.getAnimal().getAnimal())
                .sorted(Comparator.comparingDouble(animalEntity -> job.getWorkSpace().distSqr(animalEntity.blockPosition())))
                .collect(Collectors.toList());

        if (tick <= 0) {
            spawnNewAnimals();
            if (state == State.MOVING) {
                sim.setStatus("Moving Towards target");
                if (entities.size() > 0) {
                    blockPos = entities.get(0).blockPosition();
                    target = entities.get(0);
                    sim.getLookControl().setLookAt(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                }
                System.out.println(sim.blockPosition().distSqr(blockPos));
                if (sim.blockPosition().closerThan(blockPos, 6f)) {
                    state = State.ATTACKING;
                }

            } else if (state == State.ATTACKING) {
                sim.setStatus("Attacking Target");
                sim.swing(sim.getUsedItemHand());
                sim.getMainHandItem().getItem().hurtEnemy(sim.getMainHandItem(), target, sim);
                sim.doHurtTarget(target);
                if (target.isDeadOrDying()) {
                    target = null;
                    state = State.MOVING;

                    currentKillCount++;
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
                    }

                }

            } else if (state == State.RETURNING) {
                addItemsToChests();
                state = State.MOVING;
                if (!(sim.getMainHandItem().getItem() instanceof SwordItem)) {
                    sim.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

                }
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

    private boolean addItemsToChests() {
        for (BlockPos pos : chests) {
            ChestTileEntity chest = (ChestTileEntity) world.getBlockEntity(pos);
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

        return false;
    }

    @Override
    public void stop() {
        super.stop();
        sim.setStatus("Idle");
    }


    private enum State {
        MOVING,
        ATTACKING,
        RETURNING,


    }
}
