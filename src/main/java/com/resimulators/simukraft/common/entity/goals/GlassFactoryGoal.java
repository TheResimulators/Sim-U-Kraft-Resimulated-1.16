package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.entity.sim.SimInventory;
import com.resimulators.simukraft.common.jobs.JobGlassFactory;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GlassFactoryGoal extends BaseGoal<JobGlassFactory> {
    private final SimEntity sim;
    private final World world;
    private int tick;
    private int delay = 20;
    private State state = State.WAITING;
    private BlockPos targetPos;
    private boolean collected = false;
    private ArrayList<BlockPos> chests = new ArrayList<>();
    private ArrayList<BlockPos> furnaces = new ArrayList<>();

    //to make interaction happen over multiple ticks. keeps track of current furnace/chest
    private int furnaceIndex;
    private int chestIndex;
    private ArrayList<Integer> itemsToBeMoved = new ArrayList<>();

    public GlassFactoryGoal(SimEntity sim) {
        super(sim,sim.getAIMoveSpeed()*2,20);
        this.sim = sim;
        this.world = sim.world;

    }




    @Override
    public boolean shouldExecute() {
        job = (JobGlassFactory) sim.getJob();
        if (job == null) return  false;
        if (sim.getActivity() == Activity.GOING_TO_WORK){
            if (sim.getPosition().withinDistance(new Vector3d(job.getWorkSpace().getX(),job.getWorkSpace().getY(),job.getWorkSpace().getZ()),5)){
                sim.setActivity(Activity.WORKING);
                findChestAroundBlock(job.getWorkSpace());
                findFurnaceAroundBlock(job.getWorkSpace());
                return validateWorkArea();
            }
        }
        return false;
    }


    @Override
    public void startExecuting() {
        sim.setHeldItem(sim.getActiveHand(),Items.DIAMOND_SHOVEL.getDefaultInstance());
        if (!furnaces.isEmpty()){
            destinationBlock = furnaces.get(0);
            ValidateFurnaces();
            state = State.FURNACE_INTERACTION;

        }else {
            if (!validateWorkArea()){
                sim.getJob().setState(Activity.NOT_WORKING);
            }
        }
    }


    @Override
    public void tick() {
        super.tick();
        tick++;
        if (delay <= 0) {
            delay = 10;
            if (state == State.FURNACE_INTERACTION) {
                if (interactWithFurnace()) {
                    state = State.CHEST_INTERACTION;
                    getItemsToMove();
                    ValidateChests();
                    if (!chests.isEmpty()) {
                        destinationBlock = chests.get(0);
                    } else {
                        if (!validateWorkArea()) {
                            sim.getJob().setState(Activity.NOT_WORKING);
                        }
                    }
                }
            }
            if (state == State.CHEST_INTERACTION) {
                if (!collected) { // just started shift
                    getItemsToMove();
                    if (emptyInventory()) {
                        state = State.TRAVELING;
                        BlockPos sand = findSand();
                        if (sand != BlockPos.ZERO) {
                            targetPos = sand;
                            destinationBlock = sand;
                        }
                    }
                } else {
                    getItemsToMove();
                    getSandToMove();
                    if (emptyInventory()) {
                        state = State.TRAVELING;
                        BlockPos sand = findSand();
                        if (sand != BlockPos.ZERO) {
                            targetPos = sand;
                            destinationBlock = sand;
                        }
                    }
                }
            }
            if (state == State.TRAVELING) {
                if (targetPos != null) {
                    if (targetPos.withinDistance(sim.getPositionVec(),6)) {
                        state = State.COLLECTING;
                    } else {
                        destinationBlock = targetPos;
                        if (sim.getDistanceSq(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 50) {
                            targetPos = null;
                        }
                    }
                } else {
                    BlockPos sand = findSand();
                    if (sand != BlockPos.ZERO) {
                        targetPos = sand;
                        destinationBlock = sand;
                    }
                }
            }
            if (state == State.COLLECTING) {
                if (targetPos != null) {
                    if (targetPos.withinDistance(sim.getPositionVec(),6)) {
                        if (world.getBlockState(targetPos).getBlock() == Blocks.SAND) {
                            addItemToInventoryFromWorld(targetPos);
                            world.setBlockState(targetPos,Blocks.AIR.getDefaultState());
                            if (!findNextSand()) {
                                state = State.RETURNING;
                                destinationBlock = job.getWorkSpace();
                                targetPos = null;
                                collected = true;
                            }
                        } else {
                            findNextSand();
                        }
                    }else {
                        destinationBlock = targetPos;
                    }
                } else {
                    //state = State.TRAVELING;
                }
            }
            if (state == State.RETURNING) {
                if (sim.getDistanceSq(job.getWorkSpace().getX(), job.getWorkSpace().getY(), job.getWorkSpace().getZ()) < 4) {
                    state = State.SMELTING;
                }
            }

            if (state == State.SMELTING) {
                ArrayList<Integer> stacks = getSandInventory();
                if (stacks.size() > 0) {
                    int index = getBurnables();
                    if (index >= 0 || !checkFuelStatus()) {
                        if (!smeltSand(stacks, index)) {
                            state = State.CHEST_INTERACTION;
                            getSandToMove();
                        }

                    } else {
                        state = State.CHEST_INTERACTION;
                        getSandToMove();
                        getItemsToMove();
                    }
                }

            }
            if (state == State.NOTHING) {
                state = State.FURNACE_INTERACTION;
                destinationBlock = furnaces.get(0);
            }

        } else {
            delay--;
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


    public void findChestAroundBlock(BlockPos workPos){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundAndBelowPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof ChestTileEntity){
                chests.add(pos);
            }
        }
    }

    public void findFurnaceAroundBlock(BlockPos workPos){
        ArrayList<BlockPos> blocks =  BlockUtils.getBlocksAroundAndBelowPosition(workPos,5);
        for (BlockPos pos: blocks){
            if (world.getTileEntity(pos) instanceof FurnaceTileEntity){
                furnaces.add(pos);
            }
        }
    }

    private boolean interactWithFurnace(){
        BlockPos furnace = furnaces.get(furnaceIndex);
            if (world.getTileEntity(furnace) instanceof FurnaceTileEntity) {
                FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getTileEntity(furnace);
                if (tileEntity != null) {
                    ItemStack stack = tileEntity.getStackInSlot(2);
                    if (stack != ItemStack.EMPTY) {
                        tileEntity.setInventorySlotContents(2, ItemStack.EMPTY);
                        sim.getInventory().addItemStackToInventory(stack);
                    }
                }
            }

        if (furnaceIndex >= furnaces.size()-1){
            furnaceIndex = 0;
            return true;

        }else{
            furnaceIndex++;
        }
        return false;
    }

    private boolean emptyInventory() { // used directly after furnace interaction to empty the collected glass into chests
        BlockPos chest = chests.get(chestIndex);
        ItemStack newItem;
        ItemStack newChestStack;
        if (itemsToBeMoved.size() > 0){
        if (world.getTileEntity(chest) instanceof ChestTileEntity){
            ChestTileEntity chestEntity = (ChestTileEntity) world.getTileEntity(chest);
            if (chestEntity != null){
            for (int i = 0; i < chestEntity.getSizeInventory(); i++ ){
                ItemStack item = sim.getInventory().getStackInSlot(itemsToBeMoved.get(0));
                ItemStack chestStack = chestEntity.getStackInSlot(i);
                if(chestStack == ItemStack.EMPTY){
                    chestEntity.setInventorySlotContents(i,item);
                    sim.getInventory().setInventorySlotContents(itemsToBeMoved.get(0),ItemStack.EMPTY);
                    break;
                }else if(chestStack.getItem() == item.getItem()) {
                    int chestSize = chestStack.getCount();
                    int itemSize = item.getCount();
                    int chestSpace = chestStack.getMaxStackSize()-chestSize;
                    if (itemSize > chestSpace){
                        newItem = item.copy();
                        newItem.shrink(chestSpace);
                        newChestStack = chestStack.copy();
                        newChestStack.grow(chestSpace);
                    }else{
                        newItem = ItemStack.EMPTY;
                        newChestStack = chestStack.copy();
                        newChestStack.grow(itemSize);
                    }
                    chestEntity.setInventorySlotContents(i,newChestStack);
                    sim.getInventory().setInventorySlotContents(itemsToBeMoved.get(0),newItem);

                    break;
                        }


                    }
                itemsToBeMoved.remove(0);
                }
            }
            return false;
        }else{
            return true;
        }

    }

    private void getItemsToMove(){

        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++){
            ItemStack stack = sim.getInventory().getStackInSlot(i);
            if (stack.getItem() == Items.GLASS){
                itemsToBeMoved.add(i);
            }

        }

    }
    private void getSandToMove(){
        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++){
            ItemStack stack = sim.getInventory().getStackInSlot(i);
            if (stack.getItem() == Items.SAND){
                itemsToBeMoved.add(i);
            }

        }
    }

    private boolean interactWithChests(){ // this will be used to check for any extra sand in the chest to put in the furnace
        return false;

    }

    private BlockPos findSand(){
        final BlockPos[] sand = {BlockPos.ZERO};
        Iterable<BlockPos> blockPoses = BlockPos.getAllInBox(job.getWorkSpace().add(-10,-3,-10), job.getWorkSpace().add(10,5,10))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() == Blocks.SAND)
                .map(BlockPos::toImmutable)
                .sorted(Comparator.comparingDouble(blockPos ->job.getWorkSpace().distanceSq(blockPos)))
                .collect(Collectors.toCollection(ArrayList::new));
        for (BlockPos blockPos: blockPoses){
            if (world.getBlockState(blockPos).getBlock() == Blocks.SAND){
                sand[0] = blockPos;
                break;

            }
        }

        return sand[0];
    }


    private boolean findNextSand() {
        BlockPos sand = BlockPos.ZERO;
        ArrayList<BlockPos> blockPoses = BlockPos.getAllInBox(job.getWorkSpace().add(-5,-3,-5), job.getWorkSpace().add(5,5,5))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() == Blocks.SAND)
                .map(BlockPos::toImmutable)
                .sorted(Comparator.comparingDouble(blockPos ->sim.getPositionVec().squareDistanceTo(blockPos.getX(),blockPos.getY(),blockPos.getZ())))
                .collect(Collectors.toCollection(ArrayList::new));
        if (blockPoses.size() > 0) {
            sand = blockPoses.get(0);
            destinationBlock = sand;
            targetPos = destinationBlock;
        }
        return sand != BlockPos.ZERO;
    }



    private void ValidateFurnaces(){
        ArrayList<BlockPos> furnacesToBeRemoved = new ArrayList<>();
        for (BlockPos furnace: furnaces){
            if (world.getTileEntity(furnace) instanceof FurnaceTileEntity){
                FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getTileEntity(furnace);
                if (tileEntity == null){
                    furnacesToBeRemoved.remove(furnace);
                }
            }

        }
        for (BlockPos furnace: furnacesToBeRemoved){
            furnaces.remove(furnace);
        }
    }


    private void ValidateChests(){
        ArrayList<BlockPos> chestsToBeRemoved = new ArrayList<>();
        for (BlockPos furnace: chests){
            if (world.getTileEntity(furnace) instanceof FurnaceTileEntity){
                FurnaceTileEntity tileEntity = (FurnaceTileEntity) world.getTileEntity(furnace);
                if (tileEntity == null){
                    chestsToBeRemoved.remove(furnace);
                }
            }

        }
        for (BlockPos furnace: chestsToBeRemoved){
           chests.remove(furnace);
        }
    }

    private ArrayList<Integer> getSandInventory(){
        ArrayList<Integer> sandStacks = new ArrayList<>();
        for (int i = 0; i < sim.getInventory().getSizeInventory(); i++){
            ItemStack stack = sim.getInventory().getStackInSlot(i);
            if (stack.getItem() == Items.SAND){
                sandStacks.add(i);
            }
        }
    return sandStacks;
    }


    private int getBurnables(){
        int stack = -1;
        int stackIndex = -1;
        BlockPos chestSlot = BlockPos.ZERO;
        for (BlockPos pos: chests){
            ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
            if (chest != null){
                for (int i = 0; i < chest.getSizeInventory(); i++){
                    ItemStack chestStack = chest.getStackInSlot(i);
                    ItemStack currentStack;
                    if (stack != -1){
                        currentStack = chest.getStackInSlot(stack);

                        if (ForgeHooks.getBurnTime(chestStack) > ForgeHooks.getBurnTime(currentStack) && ForgeHooks.getBurnTime(chestStack) > 0){
                            stack = i;
                            chestSlot = pos;
                        }
                    } else {
                        if (ForgeHooks.getBurnTime(chestStack) > 0){
                            stack = i;
                            chestSlot = pos;
                        }
                    }
                }
            }
        }
        if (chestSlot != BlockPos.ZERO){
            ChestTileEntity chest = ((ChestTileEntity)world.getTileEntity(chestSlot));

            if (chest != null){
                ItemStack chestStack = chest.getStackInSlot(stack);
                while (chestStack.getCount() > 0){
                    for (int i = 0; i < sim.getInventory().getSizeInventory(); i++){
                        if (sim.getInventory().getStackInSlot(i).isEmpty()) {
                            sim.getInventory().setInventorySlotContents(i, chestStack.copy());
                            chestStack.shrink(chestStack.getCount());
                            stackIndex = i;
                            break;
                        }else{
                            if (chestStack.getItem() == sim.getInventory().getStackInSlot(i).getItem() && sim.getInventory().getStackInSlot(i).getCount() < sim.getInventory().getStackInSlot(i).getMaxStackSize()){
                                ItemStack simItem = sim.getInventory().getStackInSlot(i);
                                int space = simItem.getMaxStackSize() - simItem.getCount();
                                if (chestStack.getCount() > space){
                                    simItem.setCount(simItem.getMaxStackSize());
                                    chestStack.shrink(space);
                                }else {
                                    simItem.grow(chestStack.getCount());
                                    chestStack.shrink(chestStack.getCount());

                                }
                                stackIndex = i;
                                break;

                            }
                        }
                    }
                }
            }

        }
        return stackIndex;
        }


    private boolean smeltSand(ArrayList<Integer> indexs,int burnable){
        boolean success = false;
        for (BlockPos pos: furnaces){
            if (world.getTileEntity(pos) instanceof FurnaceTileEntity){
                FurnaceTileEntity tile = (FurnaceTileEntity) world.getTileEntity(pos);
                if (tile != null){
                    if (tile.getStackInSlot(0).isEmpty()){
                        tile.setInventorySlotContents(0,sim.getInventory().getStackInSlot(indexs.get(0)));
                        sim.getInventory().setInventorySlotContents(indexs.get(0),ItemStack.EMPTY);
                        indexs.remove(indexs.get(0));
                        success = true;
                    }else if(tile.getStackInSlot(0).getItem() == Items.SAND){
                        ItemStack tileStack = tile.getStackInSlot(0);
                        if (tileStack.getCount() < tileStack.getMaxStackSize()){
                            int space = tileStack.getMaxStackSize() - tileStack.getCount();
                            if (sim.getInventory().getStackInSlot(indexs.get(0)).getCount() < space){
                                tileStack.grow(sim.getInventory().getStackInSlot(indexs.get(0)).getCount());
                                sim.getInventory().setInventorySlotContents(indexs.get(0),ItemStack.EMPTY);
                            }else {
                                tileStack.grow(space);
                                sim.getInventory().getStackInSlot(indexs.get(0)).shrink(space);
                            }

                            success = true;
                        }
                    }
                    if (success) {
                        if (burnable < 0){
                            burnable = checkInventoryBurnables();
                        }
                        if (burnable >= 0) {
                            tile.setInventorySlotContents(1, sim.getInventory().getStackInSlot(burnable));
                            sim.getInventory().setInventorySlotContents(burnable, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        return success;
    }


    private boolean checkFuelStatus(){ // return true if needs fuel
        for (BlockPos pos: furnaces){
            FurnaceTileEntity furnace = (FurnaceTileEntity) world.getTileEntity(pos);
            if (furnace != null){
                if (furnace.getStackInSlot(1).isEmpty()){
                    return true;
                }

            }

        }
        return false;


    }
    private boolean validateWorkArea(){
        Faction faction = SavedWorldData.get(world).getFactionWithSim(sim.getUniqueID());
        boolean valid = true;
        if (job.getWorkSpace() != null){
        if (chests.isEmpty()){
            faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) has no Inventory at " + job.getWorkSpace(), world);
            valid = false;
        }
        else if (furnaces.isEmpty()){
            faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) Has no Furnaces to smelt with at " + job.getWorkSpace(), world);
            valid = false;
        }
        else{
            faction.sendFactionChatMessage(sim.getDisplayName().getString() + " (Glass Factory) has started work at " + job.getWorkSpace(), world);
            }
        }else{
            return false;
        }
        return valid;
    }

    private int checkInventoryBurnables(){
        int stack = -1;
        SimInventory inv = sim.getInventory();
                for (int i = 0; i < inv.getSizeInventory(); i++){
                    ItemStack invStack = inv.getStackInSlot(i);
                    ItemStack currentStack;
                    if (stack != -1){
                        currentStack = inv.getStackInSlot(stack);
                        if (ForgeHooks.getBurnTime(invStack) > ForgeHooks.getBurnTime(currentStack) && ForgeHooks.getBurnTime(invStack) > 0){
                            stack = i;
                        }
                    }else {
                        if (ForgeHooks.getBurnTime(invStack) > 0){
                            stack = i;
                        }
                    }
                }
        return stack;
    }

    private void addItemToInventoryFromWorld(BlockPos pos){
        SimInventory inventory = sim.getInventory();
        BlockState above = world.getBlockState(pos.up());

        if (above.getBlock() == Blocks.AIR){
            pos = pos.up();
        }
        Block block = world.getBlockState(pos).getBlock();
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) sim.getEntityWorld())
                .withRandom(world.rand)
                .withParameter(LootParameters.TOOL, sim.getActiveItemStack())
                .withNullableParameter(LootParameters.BLOCK_ENTITY, world.getTileEntity(pos));
        List<ItemStack> drops = block.getDefaultState().getDrops(builder);

        for (ItemStack stack : drops) {
            sim.getInventory().addItemStackToInventory(stack);
        }
    }

    @Override
    public void resetTask() {
        sim.setHeldItem(sim.getActiveHand(),ItemStack.EMPTY);
    }

    private enum State {
        TRAVELING,
        COLLECTING,
        SMELTING,
        WAITING,
        RETURNING,
        NOTHING,
        CHEST_INTERACTION,
        FURNACE_INTERACTION


    }
}
