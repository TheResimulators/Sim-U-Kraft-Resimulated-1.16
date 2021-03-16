package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.jobs.JobBuilder;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.tileentity.TileResidential;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModBlockProperties;
import com.resimulators.simukraft.init.ModBlocks;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BuilderGoal extends BaseGoal<JobBuilder> {
    private final SimEntity sim;
    private int tick;
    private BuildingTemplate template;
    private List<Template.BlockInfo> blocks;
    private State state = State.STARTING;
    private int blockIndex = 0;
    private int delay = 20;
    private int notifyDelay = 0;
    Rotation rotation;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private HashMap<Item,Integer> blocksNeeded = new HashMap<>();
    public BuilderGoal(SimEntity sim) {
        super(sim, .7d, 20);
        this.sim = sim;
    }

    @Override
    public boolean shouldExecute() {
        job = ((JobBuilder) sim.getJob());
        //System.out.println("startExecuting");
        if (job != null) {
            template = job.getTemplate();
            if (sim.getActivity() == Activity.GOING_TO_WORK) {
                checkForInventories();
                if (chests.size() != 0){
                    if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()), 5)) {
                        sim.setActivity(Activity.WORKING);
                        return template != null;
                    }
                }
                else {
                    if (notifyDelay <= 0) {
                        SavedWorldData.get(sim.world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage(sim.getName().getString() + " Builder needs a chest to start Building",sim.world);
                        notifyDelay = 2000;
                    }else{
                        notifyDelay--;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        template = job.getTemplate();

        if (template != null) {

            Rotation orgDir = getRotation(template.getDirection());
            Rotation facing = getRotation(job.getDirection());
            rotation = getRotationCalculated(orgDir,facing);

            PlacementSettings settings = new PlacementSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE);
            blocks = StructureHandler.modifyAndConvertTemplate(template, sim.world, sim.getJob().getWorkSpace().offset(job.getDirection()),settings);
            SimuKraft.LOGGER().debug("cost: " + template.getCost());
            setBlocksNeeded();
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
                /*
                 setBlocksNeeded();
                 retrieveItemsFromChest();
                 un-comment for official release
                */
                state = State.TRAVELING;
                destinationBlock = blocks.get(blockIndex).pos;
                while (sim.getEntityWorld().getBlockState(destinationBlock) ==blocks.get(blockIndex).state){
                    blockIndex++;
                    if (blockIndex < blocks.size()) {
                        destinationBlock = blocks.get(blockIndex).pos;
                    }
                }
            }
            if (state == State.TRAVELING){
                if(sim.getEntityWorld().getBlockState(destinationBlock) ==blocks.get(blockIndex).state){
                    destinationBlock = blocks.get(blockIndex).pos;
                }
                if (sim.getDistanceSq(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()) < 20){
                    state = State.BUILDING;
                    return;
                }
            }
            if (state == State.BUILDING){
                Template.BlockInfo blockInfo = blocks.get(blockIndex);
                BlockState blockstate = blockInfo.state;
                if (blockInfo.state.getBlock() == ModBlocks.CONTROL_BLOCK.get()) {
                    blockstate = blockInfo.state.with(ModBlockProperties.TYPE, template.getTypeID());
                    template.setControlBlock(blockInfo.pos);
                }
                if (sim.getInventory().hasItemStack(new ItemStack(blockInfo.state.getBlock())) || true){ // remove true for official release. for testing purposes
                    //BlockState blockState = sim.world.getBlockState(blockInfo.pos);
                    sim.world.destroyBlock(blockInfo.pos,true);
                    sim.world.setBlockState(blockInfo.pos, blockstate.rotate(sim.world,blockInfo.pos,rotation), 3);
                    int index = sim.getInventory().findSlotMatchingUnusedItem(new ItemStack(blockInfo.state.getBlock()));
                    if (index >= 0){
                    sim.getInventory().decrStackSize(index,1);}
                    blockIndex++;
                    if (blockIndex < blocks.size() - 1) {
                        destinationBlock = blocks.get(blockIndex).pos;
                        sim.getNavigator().setPath(null, 7d);
                        state = State.TRAVELING;
                    }
                }else{
                    state = State.COLLECTING;
                    destinationBlock = job.getWorkSpace();
                }
            }

            if (state == State.COLLECTING){
                if (sim.getDistanceSq(destinationBlock.getX(),destinationBlock.getY(),destinationBlock.getZ()) < 10){
                    retrieveItemsFromChest();
                    state = State.STARTING;
                }

            }
        } else {
            delay--;
        }
        if (blockIndex >= blocks.size()){
            sim.getJob().setState(Activity.FORCE_STOP);

            Faction faction = SavedWorldData.get(sim.getEntityWorld()).getFactionWithSim(sim.getUniqueID());
            faction.sendFactionChatMessage("Builder " + sim.getName().getString() + "has finished building " + template.getName(), sim.getEntityWorld());
            if (template.getTypeID() == BuildingType.RESIDENTIAL.id) {
                BlockPos controlBlock = template.getControlBlock();
                UUID id = faction.addNewHouse(controlBlock, template.getName(), template.getRent());
                TileResidential tile =(TileResidential) sim.getEntityWorld().getTileEntity(controlBlock);
                tile.setFactionID(faction.getId());
                tile.setHouseID(id);

                blockIndex = 0;

                template = null;
                sim.fireSim(sim, faction.getId(), false);
            }
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

    private Rotation getRotation(Direction dir){
        switch (dir){
            case NORTH:
                return Rotation.CLOCKWISE_180;
            case SOUTH:
                return Rotation.NONE;
            case WEST:
                return Rotation.CLOCKWISE_90;
            case EAST:
                return Rotation.COUNTERCLOCKWISE_90;

        }
        return Rotation.NONE;

    }

    private Rotation getRotationCalculated(Rotation org, Rotation cur) {
        if (org == cur.add(Rotation.CLOCKWISE_90)){
            return Rotation.COUNTERCLOCKWISE_90;
        }
        if (org == cur.add(Rotation.COUNTERCLOCKWISE_90)){
            return Rotation.CLOCKWISE_90;
        }
        if (org == cur.add(Rotation.CLOCKWISE_180)){
            return Rotation.CLOCKWISE_180;
        }

        return Rotation.NONE;
    }


    private Direction rotateDirection(Direction dir, Rotation rotation){
        switch (rotation){
            case NONE:
                return dir;
            case CLOCKWISE_90:
                return dir.rotateY();
            case CLOCKWISE_180:
                return dir.getOpposite();
            case COUNTERCLOCKWISE_90:
                return dir.rotateYCCW();

        }

        return Direction.SOUTH;
    }
    //Checks for inventories around position.
    private void checkForInventories() {
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundAndBelowPosition(job.getWorkSpace(),5);
        blocks.addAll(BlockUtils.getBlocksAroundAndBelowPosition(job.getWorkSpace().up(),5));
        blocks = (ArrayList<BlockPos>) blocks.stream().filter(pos -> sim.world.getTileEntity(pos) != null).collect(Collectors.toList());
        for (BlockPos pos: blocks){
            if (sim.world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
    }

    private void setBlocksNeeded(){
        blocksNeeded.clear();
        for (int i = this.blockIndex; i< blocks.size();i++){
            Template.BlockInfo info = blocks.get(i);
            Block block = info.state.getBlock();
            if (block == Blocks.AIR) continue;
            if (blocksNeeded.get(block.asItem()) == null){
                blocksNeeded.put(block.asItem(),1);
            }else{
                int value = blocksNeeded.get(block.asItem());
                value += 1;
                blocksNeeded.put(block.asItem(),value);

            }

        }

    }
    //Scans inventories and makes Sim go get items from chest that contains it.
    private void retrieveItemsFromChest() {
        for (BlockPos pos: chests){
            ChestTileEntity entity = (ChestTileEntity) sim.world.getTileEntity(pos);
            for (int i = 0; i< entity.getSizeInventory();i++){
                ItemStack stack = entity.getStackInSlot(i);
                if (blocksNeeded.containsKey(stack.getItem())){
                    int amountNeeded = blocksNeeded.get(stack.getItem());

                    if (amountNeeded < stack.getCount()){
                        stack.setCount(stack.getCount()-amountNeeded);
                        blocksNeeded.remove(stack.getItem());
                    }else{
                        amountNeeded -= stack.getCount();
                        blocksNeeded.put(stack.getItem(),amountNeeded);
                        stack.setCount(0);
                    }
                    ItemStack result = stack.copy();
                    result.setCount(amountNeeded);
                    sim.getInventory().addItemStackToInventory(result);
                    entity.setInventorySlotContents(i,stack);

                }
            }

        }
        final String[] string = {""};
            blocksNeeded.keySet().forEach(key -> {
            int amount = blocksNeeded.get(key);
             string[0] += amount + " " + key + " ";

        });
            if (!string[0].equals("")){
        SavedWorldData.get(sim.world).getFactionWithSim(sim.getUniqueID()).sendFactionChatMessage(sim.getName().getString() + " still needs " + string[0],sim.world);
        delay = 2000;
    }}

    private enum State {
        STARTING,
        TRAVELING,
        BUILDING,
        WAITING,
        COLLECTING
    }
}
