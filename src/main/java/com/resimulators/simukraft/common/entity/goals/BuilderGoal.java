package com.resimulators.simukraft.common.entity.goals;

import com.mojang.authlib.GameProfile;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.jobs.old.JobBuilder;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileResidential;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModBlockProperties;
import com.resimulators.simukraft.init.ModBlocks;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BuilderGoal extends BaseGoal<JobBuilder> {
    private final SimEntity sim;
    Rotation rotation;
    private int tick;
    private BuildingTemplate template;
    private List<Template.BlockInfo> blocks;
    private State state = State.STARTING;
    private int blockIndex = 0;
    private int delay = 20;
    private int notifyDelay = 0;
    private FakePlayer player;
    private PlacementSettings settings;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final HashMap<Item, Integer> blocksNeeded = new HashMap<>();


    public BuilderGoal(SimEntity sim) {
        super(sim, .7d, 20);
        this.sim = sim;
        if (!sim.level.isClientSide) {
            player = new FakePlayer((ServerWorld) sim.level, new GameProfile(null, "Builder_" + UUID.randomUUID()));
        }
    }

    @Override
    public boolean canUse() {
        job = ((JobBuilder) sim.getJob());
        //System.out.println("startExecuting");
        if (SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()) != null) {
            if (job != null) {
                template = job.getTemplate();
                if (sim.getActivity() == Activity.GOING_TO_WORK) {
                    checkForInventories();
                    if (chests.size() != 0) {
                        if (sim.blockPosition().closerThan(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                            return template != null;
                        }
                    } else {
                        if (notifyDelay <= 0) {
                            SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(sim.getName().getString() + " Builder needs a chest to start Building", sim.level);
                            notifyDelay = 2000;
                        } else {
                            notifyDelay--;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void start() {
        sim.setActivity(Activity.WORKING);
        template = job.getTemplate();
        blockIndex = 0;
        if (template != null) {

            Rotation orgDir = getRotation(template.getDirection());
            Rotation facing = getRotation(job.getDirection());
            rotation = getRotationCalculated(orgDir, facing);

            settings = new PlacementSettings()
                    .setRotation(rotation)
                    .setMirror(template.getMirror());
            BlockPos origin = sim.getJob().getWorkSpace().offset(template.getOffSet().rotate(rotation).offset(job.getDirection().getNormal()));
            blocks = StructureHandler.modifyAndConvertTemplate(template, sim.level, origin, settings);
            adjustBlocks();
            blocks.sort(Comparator
                    .comparingInt((Template.BlockInfo info) -> !info.state.getBlock().hasDynamicShape() && info.state.isCollisionShapeFullBlock(EmptyBlockReader.INSTANCE, BlockPos.ZERO) || info.state.getBlock() instanceof AirBlock ? -1 : 1)
                    .thenComparingInt((info) -> info.pos.getY())
                    .thenComparingInt((info) -> {
                        System.out.println("Sorting " + sim.getJob().getWorkSpace().subtract(info.pos));
                        switch (job.getDirection()) {
                            case NORTH:
                                return (sim.getJob().getWorkSpace().getZ() - (info.pos.getZ()));
                            case SOUTH:
                                return (sim.getJob().getWorkSpace().getZ() - (info.pos.getZ())) * -1;
                            case WEST:
                                return (sim.getJob().getWorkSpace().getX() - (info.pos.getX()));
                            case EAST:
                                return (sim.getJob().getWorkSpace().getX() - (info.pos.getX())) * -1;
                            default:
                                return 0;
                        }
                    }).thenComparingInt((info) -> {
                        switch (job.getDirection()) {
                            case NORTH:
                                return (sim.getJob().getWorkSpace().getX() - (info.pos.getX())) * -1;
                            case SOUTH:
                                return (sim.getJob().getWorkSpace().getX() - (info.pos.getX()));
                            case WEST:
                                return (sim.getJob().getWorkSpace().getZ() - (info.pos.getZ()));
                            case EAST:
                                return (sim.getJob().getWorkSpace().getZ() - (info.pos.getZ())) * -1;
                            default:
                                return 0;
                        }
                    })
            );
            System.out.println("SORRRTTTTTED");
            blocks.forEach(blockInfo -> System.out.println(sim.getJob().getWorkSpace().subtract(blockInfo.pos)));

            //setBlocksNeeded();
            //template.placeInWorld((IServerWorld) sim.level, origin.above(10), settings, new Random());
        }
    }

    @Override
    public boolean shouldRecalculatePath() {
        return sim.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > acceptedDistance();
    }

    private Rotation getRotation(Direction dir) {
        switch (dir) {
            case NORTH:
                return Rotation.COUNTERCLOCKWISE_90;
            case SOUTH:
                return Rotation.CLOCKWISE_90;
            case WEST:
                return Rotation.CLOCKWISE_180;
            case EAST:
                return Rotation.NONE;

        }
        return Rotation.NONE;

    }

    private Rotation getRotationCalculated(Rotation org, Rotation cur) {
        if (org == cur.getRotated(Rotation.CLOCKWISE_90)) {
            return Rotation.COUNTERCLOCKWISE_90;
        }
        if (org == cur.getRotated(Rotation.COUNTERCLOCKWISE_90)) {
            return Rotation.CLOCKWISE_90;
        }
        if (org == cur.getRotated(Rotation.CLOCKWISE_180)) {
            return Rotation.CLOCKWISE_180;
        }

        return Rotation.NONE;
    }

    private void adjustBlocks() {
        List<Template.BlockInfo> newBlocks = new ArrayList<>();
        for (Template.BlockInfo block : blocks) {
            if (block.state.getBlock() == Blocks.GRASS_BLOCK) {
                Template.BlockInfo info = new Template.BlockInfo(block.pos, Blocks.DIRT.defaultBlockState(), null);
                newBlocks.add(info);
            } else {
                newBlocks.add(block);
            }
        }

        blocks = newBlocks;
    }

    //Checks for inventories around position.
    private void checkForInventories() {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundAndBelowPosition(job.getWorkSpace(), 5);
        blocks.addAll(BlockUtils.getBlocksAroundAndBelowPosition(job.getWorkSpace().above(), 5));
        blocks = (ArrayList<BlockPos>) blocks.stream().filter(pos -> sim.level.getBlockEntity(pos) != null).collect(Collectors.toList());
        for (BlockPos pos : blocks) {
            if (sim.level.getBlockEntity(pos) instanceof ChestTileEntity) {
                chests.add(pos);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (sim.getJob() != null) {
            if (sim.getJob().getActivity() == Activity.FORCE_STOP) {
                return false;
            }
            if (tick < sim.getJob().workTime()) {
                return true;
            } else {
                sim.getJob().finishedWorkPeriod();
                sim.getJob().setActivity(Activity.NOT_WORKING);
            }
        }

        return false;
    }

    @Override
    public void tick() {
        tick++;
        super.tick();
        if (delay <= 0) {
            delay = 1;
            if (state == State.STARTING) {
                setBlocksNeeded();
                //retrieveItemsFromChest();
                 /*
                 un-comment for official release
                */
                state = State.TRAVELING;
                blockPos = blocks.get(blockIndex).pos;
//                while (sim.getCommandSenderWorld().getBlockState(blockPos) ==blocks.get(blockIndex).state){
//                    blockIndex++;
//                    if (blockIndex < blocks.size()) {
//                        blockPos = blocks.get(blockIndex).pos;
//                    }
//                }
            }
            if (state == State.TRAVELING) {

                if (sim.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) < 20) {
                    state = State.BUILDING;
                    return;
                } else {
                    sim.level.setBlock(blockPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
            }
            if (state == State.BUILDING) {
                if (blockIndex < blocks.size()) {
                    Template.BlockInfo blockInfo = blocks.get(blockIndex);
                    BlockState blockstate = blockInfo.state;
                    blockstate = blockstate.rotate(sim.level, blockInfo.pos, rotation.getRotated(template.getBlockRotation()));

                    if ((sim.getInventory().hasItemStack(new ItemStack(blockInfo.state.getBlock())) || blockIgnorable(blockstate)) || true) { // remove true for official release. for testing purposes
                        if (placeBlock(blockInfo, blockstate)) {

                            int index = sim.getInventory().findSlotMatchingUnusedItem(new ItemStack(blockstate.getBlock()));
                            if (index >= 0) {
                                sim.getInventory().removeItem(index, 1);
                            }
                            //blockIndex++;
                        } else {
                            state = State.COLLECTING;
                            blockPos = job.getWorkSpace();
                        }

                        if (blockIndex < blocks.size() - 1) {
                            blockPos = blocks.get(blockIndex).pos;
                            sim.getNavigation().moveTo((Path) null, 7d);
                            state = State.TRAVELING;
                        }
                    }
                }
            }


            if (state == State.COLLECTING) {
                if (sim.distanceToSqr(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()) < 10) {
                    retrieveItemsFromChest();
                    state = State.STARTING;
                } else {
                    blockPos = job.getWorkSpace();
                }

            }
        } else {
            delay--;
        }
        if (blockIndex >= blocks.size()) {
            sim.getJob().setActivity(Activity.FORCE_STOP);

            Faction faction = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID());
            faction.sendFactionChatMessage("Builder " + sim.getName().getString() + "has finished building " + template.getName(), sim.getCommandSenderWorld());
            if (template.getTypeID() == BuildingType.RESIDENTIAL.id) {
                BlockPos controlBlock = template.getControlBlock();
                UUID id = faction.addNewHouse(controlBlock, template.getName(), template.getRent());
                TileResidential tile = (TileResidential) sim.level.getBlockEntity(controlBlock);
                if (tile != null) {
                    tile.setFactionID(faction.getId());
                    tile.setHouseID(id);
                    sim.level.markAndNotifyBlock(tile.getBlockPos(), sim.level.getChunkAt(tile.getBlockPos()), tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.DEFAULT, 512);
                }
                blockIndex = 0;

                template = null;
                sim.fireSim(sim, faction.getId(), false);
            }
        }
    }

    @Override
    protected boolean isValidTarget(IWorldReader iWorldReader, BlockPos blockPos) {
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        blockIndex = 0;
    }

    private void setBlocksNeeded() {
        blocksNeeded.clear();
        for (int i = this.blockIndex; i < blocks.size(); i++) {
            Template.BlockInfo info = blocks.get(i);
            Block block = info.state.getBlock();
            if (block == Blocks.AIR) continue;
            if (block instanceof BedBlock && info.state.getValue(BedBlock.PART) == BedPart.HEAD) continue;
            if (block instanceof DoorBlock) {
                if (info.state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                    continue;
            }
            if (blocksNeeded.get(block.asItem()) == null) {
                blocksNeeded.put(block.asItem(), 1);
            } else {
                int value = blocksNeeded.get(block.asItem());
                value += 1;
                blocksNeeded.put(block.asItem(), value);

            }

        }

    }

    private boolean blockIgnorable(BlockState state) {
        return (state.getBlock() == Blocks.AIR
                || state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD
                || state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER);

    }

    private boolean placeBlock(Template.BlockInfo blockInfo, BlockState blockstate) {

        if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BLOCK.get()) {
            blockstate = blockInfo.state.setValue(ModBlockProperties.TYPE, template.getTypeID());
            template.setControlBlock(blockInfo.pos);
        }
        if (blockstate.getBlockState().getBlock().equals(sim.level.getBlockState(blockInfo.pos).getBlock())) {
            blockIndex++;
            return false;
        }
        if (blockstate.getBlock() instanceof BedBlock) {
            if (blockstate.getValue(BedBlock.PART) == BedPart.HEAD) {
                blockIndex++;
                return false;
            }
        }

        sim.level.getBlockState(blockPos).getBlock().removedByPlayer(sim.level.getBlockState(blockPos), sim.level, blockPos, player, true, sim.level.getFluidState(blockPos));
        sim.level.setBlock(blockPos, blockstate, 2 & -2 | 16);
        blockstate.getBlock().setPlacedBy(sim.level, blockInfo.pos, blockstate, null, ItemStack.EMPTY);

        if (!settings.getKnownShape()) {
            BlockState blockstate1 = sim.level.getBlockState(blockPos);
            BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate1, sim.level, blockPos);
            if (blockstate1 != blockstate3) {
                sim.level.setBlock(blockPos, blockstate3, 2 & -2 | 16);
            }
            sim.level.blockUpdated(blockPos, blockstate3.getBlock());
            blockIndex++;
            return true;
        }


        blockIndex++;
        return false;
    }

    //Scans inventories and makes Sim go get items from chest that contains it.
    private void retrieveItemsFromChest() {
        for (BlockPos pos : chests) {
            ChestTileEntity entity = (ChestTileEntity) sim.level.getBlockEntity(pos);
            if (entity != null) {
                for (int i = 0; i < entity.getContainerSize(); i++) {
                    ItemStack stack = entity.getItem(i);
                    if (blocksNeeded.containsKey(stack.getItem())) {
                        int amountNeeded = blocksNeeded.get(stack.getItem());

                        if (amountNeeded < stack.getCount()) {
                            stack.setCount(stack.getCount() - amountNeeded);
                            blocksNeeded.remove(stack.getItem());
                        } else {
                            amountNeeded -= stack.getCount();
                            blocksNeeded.put(stack.getItem(), amountNeeded);
                            stack.setCount(0);
                        }
                        ItemStack result = stack.copy();
                        result.setCount(amountNeeded);
                        sim.getInventory().addItemStackToInventory(result);
                        entity.setItem(i, stack);

                    }
                }
            }
        }
        final String[] string = {""};
        HashMap<Item, Integer> blocksNeededCache = new HashMap<>(blocksNeeded);
        blocksNeededCache.keySet().forEach(key -> {
            if (key.equals(ModBlocks.CONTROL_BLOCK.get().asItem())) {
                blocksNeeded.remove(key);
            } else {
                int amount = blocksNeeded.get(key);
                string[0] += amount + " " + key + ", ";
            }
        });
        if (!string[0].equals("")) {
            SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID()).sendFactionChatMessage(sim.getName().getString() + " still needs " + string[0], sim.level);
            delay = 2000;
        }
    }

    private Direction rotateDirection(Direction dir, Rotation rotation) {
        switch (rotation) {
            case NONE:
                return dir;
            case CLOCKWISE_90:
                return dir.getClockWise();
            case CLOCKWISE_180:
                return dir.getOpposite();
            case COUNTERCLOCKWISE_90:
                return dir.getCounterClockWise();

        }

        return Direction.SOUTH;
    }

    private enum State {
        STARTING,
        TRAVELING,
        BUILDING,
        WAITING,
        COLLECTING
    }

}
