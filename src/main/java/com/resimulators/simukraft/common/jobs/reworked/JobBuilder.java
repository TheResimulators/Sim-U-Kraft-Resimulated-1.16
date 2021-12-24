package com.resimulators.simukraft.common.jobs.reworked;

import com.mojang.authlib.GameProfile;
import com.resimulators.simukraft.common.block.BlockControlBlock;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class JobBuilder implements IReworkedJob {
    private final SimEntity sim;
    Rotation rotation;
    private BuildingTemplate template;
    private int periodsworked = 0;
    private BlockPos workSpace;
    private Activity activity = Activity.NOT_WORKING;
    private Direction direction;
    private boolean finished;
    private BlockPos blockPos;

    private List<Template.BlockInfo> blocks;
    private State state = State.STARTING;
    private int blockIndex = 0;
    private int delay = 20;
    private int notifyDelay = 10;
    private FakePlayer player;
    private PlacementSettings settings;
    private final ArrayList<BlockPos> chests = new ArrayList<>();
    private final HashMap<Item, Integer> blocksNeeded = new HashMap<>();
    private TileConstructor constructor;
    private Faction faction;

    private int itemCount = 0;
    public JobBuilder(SimEntity sim) {
        this.sim = sim;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Profession jobType() {
        return Profession.BUILDER;
    }

    @Override
    public int intervalTime() {
        return 200;
    }

    @Override
    public int workTime() {
        return 12000;
    }

    @Override
    public int maximumWorkPeriods() {
        return -1;
        //negative one so that it can work as much as it can. builder should work all day.
        // if it can't find resources it take a 1000 tick break
    }
    @Override
    public boolean nightShift() {
        return false;
    }

    @Override
    public int getPeriodsWorked() {
        return periodsworked;
    }

    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT data = new CompoundNBT();
        data.putInt("id", sim.getProfession());
        nbt.add(data);
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);
        CompoundNBT other = new CompoundNBT(); // other info that is unique to the miner
        if (workSpace != null) {
            other.putLong("jobpos", workSpace.asLong());
        }
        other.putInt("blockIndex",blockIndex);
        other.putBoolean("finished", finished);
        other.putInt("item count", itemCount);
        nbt.add(other);

        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsworked = list.getInt("periodsworked");
            }
            if (list.contains("jobpos")) {
                setWorkSpace(BlockPos.of(list.getLong("jobpos")));
            }
            if (list.contains("finished")) {
                finished = list.getBoolean("finished");
            }
            if (list.contains("blockIndex")){
                blockIndex = list.getInt("blockIndex");
            }
            if (list.contains("item count")){
                itemCount = list.getInt("item count");
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(periodsworked++);
    }

    @Override
    public void setWorkedPeriods(int periods) {
        periodsworked = periods;
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

    /** not using as builder is based on blocks placed. moved to the tile entity to centralize the calculations of it and save in nbt*/
    @Override
    public double getWage() {
        return 0;
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
        if (player == null && !sim.level.isClientSide) {
            player = new FakePlayer((ServerWorld) sim.level, new GameProfile(null, "Builder_" + sim.getUUID()));
        }
        faction = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID());
        checkForInventories();
        sim.setActivity(Activity.WORKING);
        blockIndex = 0;
        blockPos = getWorkSpace();
        if (template != null) {
            Rotation orgDir = getRotation(template.getDirection());
            Rotation facing = getRotation(getDirection());
            rotation = getRotationCalculated(orgDir, facing);
            settings = new PlacementSettings()
                    .setRotation(rotation)
                    .setMirror(template.getMirror());
            BlockPos origin = sim.getJob().getWorkSpace().offset(template.getOffSet().rotate(rotation).offset(getDirection().getNormal()));
            blocks = StructureHandler.modifyAndConvertTemplate(template, sim.level, origin, settings);
            adjustBlocks();
            sortBlocks();

            constructor = (TileConstructor) sim.level.getBlockEntity(workSpace);
            constructor.setBuildingPositioning(blocks.get(blocks.size()-1).pos,direction);
            chargeBlockIndexForward();
            constructor.onStartBuilding(blockIndex+1,blocks.size());
        }
    }

    @Override
    public void tick() {
        if (template == null)
            return;
        if (blocks == null)
            start();
        checkForInventories();
        if (delay <= 0) {
            delay = 2;
            if (state == State.STARTING) {
                setBlocksNeeded();
                constructor.setBlocksNeeded(blocksNeeded);
                retrieveItemsFromChest();
                state = State.TRAVELING;
                blockIndex = 0;
                chargeBlockIndexForward();
            }
            if (state == State.TRAVELING) {
                if (Math.sqrt(sim.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())) < 8) {
                    state = State.BUILDING;
                    blockPos = blocks.get(blockIndex).pos;
                } else {
                    sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
                }
            }
            if (state == State.BUILDING) {
                if (blockIndex < blocks.size()) {
                    Template.BlockInfo blockInfo = blocks.get(blockIndex);
                    BlockState blockstate = blockInfo.state;
                    blockstate = blockstate.rotate(sim.level, blockInfo.pos, rotation.getRotated(template.getBlockRotation()));


                    if ((blockInfo.state.getBlock() instanceof BlockControlBlock || sim.getInventory().hasItemStack(new ItemStack(blockInfo.state.getBlock()))) || (blockIgnorable(blockstate))) {
                            int index = sim.getInventory().findSlotMatchingUnusedItem(new ItemStack(blockstate.getBlock()));
                        if (index >= 0 ) {
                            if (placeBlock(blockInfo, blockstate) ) {
                                sim.getInventory().removeItem(index, 1);
                                constructor.updateBlockIndex(blockIndex+1);
                                itemCount++;
                                updateWage();
                            }
                        }else if (blockInfo.state.getBlock() instanceof BlockControlBlock){
                            if (placeBlock(blockInfo, blockstate)){
                                constructor.updateBlockIndex(blockIndex+1);
                            }
                        }else if (blockIgnorable(blockstate)){
                            if (placeBlock(blockInfo, blockstate)){
                                constructor.updateBlockIndex(blockIndex+1);
                                }
                            }
                        updateIndex();
                    } else {
                        
                        state = State.COLLECTING;
                        blockPos = getWorkSpace();
                    }
                }
            }


            if (state == State.COLLECTING) {
                if (sim.distanceToSqr(getWorkSpace().getX(), getWorkSpace().getY(), getWorkSpace().getZ()) < 10) {
                    setBlocksNeeded();
                    retrieveItemsFromChest();
                    state = State.STARTING;
                } else {
                    blockPos = getWorkSpace();
                    sim.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), sim.getSpeed() * 2);
                }
            }
        } else {
            delay--;
        }
        // when the builder has finished the building
        if (blocks != null && blockIndex >= blocks.size()) {
            sim.getJob().setActivity(Activity.FORCE_STOP);

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
                if (constructor.getWagePayed() < template.getCost()){
                    float dif = (template.getCost()) -constructor.getWagePayed();
                    constructor.addToWage(dif);
                }
                template = null;
            }
            sim.fireSim(sim, faction.getId(), false);
        }
    }

    public void setTemplate(BuildingTemplate template) {
        this.template = template;
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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
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


    private void updateIndex(){
        if (blockIndex < blocks.size() - 1) {
            blockPos = blocks.get(blockIndex).pos;
            //sim.getNavigation().moveTo((Path) null, 7d);
            state = State.TRAVELING;
        }
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

    private void setBlocksNeeded() {

        blocksNeeded.clear();
        chargeBlockIndexForward();
        for (int i = this.blockIndex; i < blocks.size(); i++) {
            Template.BlockInfo info = blocks.get(i);
            Block block = info.state.getBlock();
            if (block == Blocks.AIR) continue;
            if (block == ModBlocks.CONTROL_BLOCK.get()) continue;
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

    private int chargeBlockIndexForward() {
        if (blocks == null || blockIndex >= blocks.size())
            return -1;
        blockPos = blocks.get(blockIndex).pos;
        BlockState targetState = blocks.get(blockIndex).state;
        targetState = targetState.rotate(sim.level, blocks.get(blockIndex).pos, rotation.getRotated(template.getBlockRotation()));
        while (sim.getCommandSenderWorld().getBlockState(blockPos).getBlock() instanceof BlockControlBlock || sim.getCommandSenderWorld().getBlockState(blockPos).is(targetState.getBlock())) {
            blockIndex++;
            if (blockIndex < blocks.size()) {
                blockPos = blocks.get(blockIndex).pos;
                targetState = blocks.get(blockIndex).state;
                targetState = targetState.rotate(sim.level, blocks.get(blockIndex).pos, rotation.getRotated(template.getBlockRotation()));
            } else return blockIndex;
        }
        return blockIndex;
    }

    private boolean placeBlock(Template.BlockInfo blockInfo, BlockState blockstate) {

        if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BLOCK.get()) {
            blockstate = blockInfo.state.setValue(ModBlockProperties.TYPE, template.getTypeID());
            template.setControlBlock(blockInfo.pos);
        }
        if (blockstate.getBlockState().getBlock().equals(sim.level.getBlockState(blockInfo.pos).getBlock())) {
            blockIndex++;
            chargeBlockIndexForward();
            return false;
        }
        if (blockstate.getBlock() instanceof BedBlock) {
            if (blockstate.getValue(BedBlock.PART) == BedPart.HEAD) {
                blockIndex++;
                chargeBlockIndexForward();
                return false;
            }
        }

        sim.level.getBlockState(blockPos).getBlock().removedByPlayer(sim.level.getBlockState(blockPos), sim.level, blockPos, player, true, sim.level.getFluidState(blockPos));
        sim.level.setBlock(blockPos, blockstate, 2 & -2 | 16);
        blockstate.getBlock().setPlacedBy(sim.level, blockInfo.pos, blockstate, null, ItemStack.EMPTY);

        if (!settings.getKnownShape()) {
            BlockState blockstate1 = sim.level.getBlockState(blockPos);
            sim.level.updateNeighborsAt(blockPos,blockstate.getBlock());
            BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate1, sim.level, blockPos);

            if (!blockstate1.equals(blockstate3)) {
                sim.level.setBlock(blockPos, blockstate3, 2 & -2 | 16);
            }
            sim.level.blockUpdated(blockPos, blockstate3.getBlock());
            blockstate3.updateIndirectNeighbourShapes(sim.level, blockPos, 2 & -2 | 16, 512 - 1);
            blockstate3.updateNeighbourShapes(sim.level, blockPos, 2 & -2 | 16, 512 - 1);
            blockstate3.updateIndirectNeighbourShapes(sim.level, blockPos, 2 & -2 | 16, 512 - 1);
            blockIndex++;
            chargeBlockIndexForward();

            return true;
        }
        blockIndex++;
        chargeBlockIndexForward();
        return false;
    }

    private int countSimItem(Item item) {
        int count = 0;
        for (int i = 0; i < sim.getInventory().mainInventory.size(); i++) {
            if (sim.getInventory().mainInventory.get(i).getItem().equals(item))
                count += sim.getInventory().mainInventory.get(i).getCount();
        }
        return count;
    }

    //Scans inventories and makes Sim go get items from chest that contains it.
    private void retrieveItemsFromChest() {
        for (int i = blocksNeeded.size() - 1; i >= 0; i--) {
            Item item = (Item) blocksNeeded.keySet().toArray()[i];
            int needed = blocksNeeded.get(item);
            int count = countSimItem(item);
            if (count > 0)
                if (count >= needed){
                    blocksNeeded.remove(item);
                    constructor.removeBlockFromNeeded(item);}
                else{
                    blocksNeeded.put(item, needed - count);}
        }
        for (BlockPos pos : chests) {
            if (sim.level.getBlockEntity(pos) instanceof ChestTileEntity) {
                ChestTileEntity entity = (ChestTileEntity) sim.level.getBlockEntity(pos);
                if (entity != null) {
                    for (int i = 0; i < entity.getContainerSize(); i++) {
                        ItemStack stack = entity.getItem(i);
                        if (blocksNeeded.containsKey(stack.getItem())) {
                            Item item = stack.getItem();
                            int amountNeeded = blocksNeeded.get(stack.getItem());

                            if (amountNeeded < stack.getCount()) {
                                stack.setCount(stack.getCount() - amountNeeded);
                                blocksNeeded.remove(item);
                                constructor.removeBlockFromNeeded(item);
                                sim.getInventory().addItemStackToInventory(new ItemStack(item, amountNeeded));
                            } else {
                                amountNeeded -= stack.getCount();
                                blocksNeeded.put(stack.getItem(), amountNeeded);
                                constructor.removeBlockFromNeeded(item,stack.getCount());
                                sim.getInventory().addItemStackToInventory(new ItemStack(item, stack.getCount()));
                                stack.setCount(0);
                            }
                            entity.setItem(i, stack);
                        }
                    }
                }
            }
        }
        final String[] string = {""};
        HashMap<Item, Integer> blocksNeededCache = new HashMap<>(blocksNeeded);
        blocksNeededCache.keySet().forEach(key -> {
            int amount = blocksNeeded.get(key);
            if (amount > 0)
                string[0] += amount + " " + key.getDescription().getString() + ", \n";
        });
        if (!string[0].equals("") && notifyDelay < 0) {
            faction.sendFactionChatMessage(sim.getName().getString() + " still needs " + string[0], sim.level);
            notifyDelay = 2400;
        }else if (notifyDelay >= 0){
            notifyDelay--;
        } else {
            notifyDelay = 10;
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

    //Checks for inventories around position.
    private void checkForInventories() {
        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundAndBelowPosition(getWorkSpace(), 2);
        blocks.addAll(BlockUtils.getBlocksAroundAndBelowPosition(getWorkSpace().above(), 2));
        blocks = (ArrayList<BlockPos>) blocks.stream().filter(pos -> sim.level.getBlockEntity(pos) != null).collect(Collectors.toList());
        for (BlockPos pos : blocks) {
            if (!chests.contains(pos) && sim.level.getBlockEntity(pos) instanceof ChestTileEntity) {
                chests.add(pos);
            }
        }
    }

    private void sortBlocks(){
        blocks.sort(Comparator
            .comparingInt((Template.BlockInfo info) -> !info.state.getBlock().hasDynamicShape() && info.state.isCollisionShapeFullBlock(EmptyBlockReader.INSTANCE, BlockPos.ZERO) || info.state.getBlock() instanceof AirBlock ? -1 : 1)
            .thenComparingInt((info) -> info.pos.getY())
            .thenComparingInt((info) -> {
                switch (getDirection()) {
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
                switch (getDirection()) {
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
    }


    private void updateWage(){
        if (constructor.getWagePayed() < constructor.getWagePerBlock() * itemCount){
            constructor.addToWage();
            faction.subCredits(constructor.getWagePerBlock());
        }
    }
    private enum State {
        STARTING,
        TRAVELING,
        BUILDING,
        WAITING,
        COLLECTING
    }

}
