package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.building.CustomTemplateManager;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.init.ModTileEntities;
import com.resimulators.simukraft.packets.BuildingsPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    private UUID simId;
    private BlockPos cornerPosition;
    private BlockPos origin;
    private boolean shouldRender;
    private boolean isBuilding;
    private int currentBlockIndex;
    private int totalBlockIndex;

    private float wagePayed = 0;
    private float wageTotal = 0;
    private float wagePerBlock = 0;


    private HashMap<Item, Integer> blocksNeeded = new HashMap<>();

    public TileConstructor() {
        super(ModTileEntities.CONSTRUCTOR.get());
    }

    @Override
    public boolean getHired() {
        return hired;
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        setChanged();
    }

    @Override
    public UUID getSimId() {
        return simId;
    }

    @Override
    public void setSimId(UUID id) {
        this.simId = id;
        setChanged();
    }

    @Override
    public void fireSim() {
        setHired(false);
        setSimId(null);
        shouldRender = false;
        isBuilding = false;
        wagePayed = 0;
        wageTotal = 0;
        wagePerBlock = 0;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUUID("simid");
        }
        if (nbt.contains("origin")) {
            origin = BlockPos.of(nbt.getLong("origin"));
            cornerPosition = BlockPos.of(nbt.getLong("cornerPos"));
        }
        if (nbt.contains("shouldRender")) {
            shouldRender = nbt.getBoolean("shouldRender");
        }
        if (nbt.contains("isbuilding")) {
            isBuilding = nbt.getBoolean("isbuilding");
        }
        if (nbt.contains("currentBlockIndex")) {
            currentBlockIndex = nbt.getInt("currentBlockIndex");
        }
        if (nbt.contains("totalBlockIndex")) {
            totalBlockIndex = nbt.getInt("totalBlockIndex");
        }
        if (nbt.contains("items needed")) {
            ListNBT listNBT = nbt.getList("items needed", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < listNBT.size(); i++) {
                CompoundNBT compoundNBT = (CompoundNBT) listNBT.get(i);
                Item item = Item.byId(compoundNBT.getInt("item"));
                int amount = compoundNBT.getInt("amount");
                blocksNeeded.put(item, amount);
            }
        }
        if (nbt.contains("wage payed")) {
            wagePayed = nbt.getFloat("wage payed");
        }
        if (nbt.contains("wage total")) {
            wageTotal = nbt.getFloat("wage total");
        }
        if (nbt.contains("wage per block")) {
            wagePerBlock = nbt.getFloat("wage per block");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putBoolean("hired", hired);
        if (simId != null) {
            nbt.putUUID("simid", simId);
        }
        if (this.origin != null) {
            nbt.putLong("origin", origin.asLong());
            nbt.putLong("cornerPos", cornerPosition.asLong());
        }
        nbt.putBoolean("shouldRender", shouldRender);
        nbt.putBoolean("isbuilding", isBuilding);
        nbt.putInt("currentBlockIndex", currentBlockIndex);
        nbt.putInt("totalBlockIndex", totalBlockIndex);
        ListNBT blocksNeededList = new ListNBT();
        for (Item item : blocksNeeded.keySet()) {
            CompoundNBT compoundNBT = new CompoundNBT();
            compoundNBT.putInt("item", Item.getId(item));
            compoundNBT.putInt("amount", blocksNeeded.get(item));
            blocksNeededList.add(compoundNBT);
        }
        nbt.put("items needed", blocksNeededList);
        nbt.putFloat("wage payed",wagePayed);
        nbt.putFloat("wage total", wageTotal);
        nbt.putFloat("wage per block",wagePerBlock);
        return nbt;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    public void FindAndLoadBuilding(PlayerEntity playerEntity) {
        if (CustomTemplateManager.isInitialized()) {
            ArrayList<BuildingTemplate> templates = CustomTemplateManager.getAllBuildingTemplates();
            Network.getNetwork().sendToPlayer(new BuildingsPacket(templates), (ServerPlayerEntity) playerEntity);
        }
    }


    public void setBuildingPositioning(BlockPos size, Direction direction) {
        cornerPosition = size;
        //BlockPos.ZERO.relative(direction).relative(direction.getClockWise(),size.getX()).relative(direction,size.getZ()).above(size.getY());
        this.origin = this.getBlockPos().relative(direction);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        setChanged();

    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public BlockPos getCornerPosition() {
        return cornerPosition;
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        this.load(blockState, parentNBTTagCompound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (cornerPosition != null) {
            return new AxisAlignedBB(this.getBlockPos(), cornerPosition);
        } else {
            return super.getRenderBoundingBox();
        }
    }

    /**
     * this returns the boolean should render
     */
    public boolean isShouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);

    }

    public boolean isBuilding() {
        return isBuilding;
    }

    public void setBuilding(boolean building) {
        isBuilding = building;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public void onStartBuilding(int buildIndex, int maxBlockIndex) {
        this.currentBlockIndex = buildIndex;
        this.totalBlockIndex = maxBlockIndex;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public void updateBlockIndex(int newBlockIndex) {
        this.currentBlockIndex = newBlockIndex;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public void removeBlockFromNeeded(Item item) {
        blocksNeeded.remove(item);
    }

    public void removeBlockFromNeeded(Item item, int amount) {
        int currentAmount = 0;
        if (blocksNeeded.containsKey(item)) {
            currentAmount = blocksNeeded.get(item);
        }
        blocksNeeded.put(item, currentAmount - amount);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public HashMap<Item, Integer> getBlocksNeeded() {
        return blocksNeeded;
    }

    public void setBlocksNeeded(HashMap<Item, Integer> blocksNeeded) {
        this.blocksNeeded = new HashMap<>(blocksNeeded);
    }

    public int getCurrentBlockIndex() {
        return currentBlockIndex;
    }

    public int getTotalBlockIndex() {
        return totalBlockIndex;
    }

    public void setWageTotal(float wageTotal) {
        this.wageTotal = wageTotal;
    }

    public void setWagePerBlock(float wagePerBlock) {
        this.wagePerBlock = wagePerBlock;
    }

    public float getWageTotal() {
        return wageTotal;
    }

    public void addToWage(){
        wagePayed += wagePerBlock;
    }
    public void addToWage(float amount){
        wagePayed += amount;
    }

    public float getWagePayed() {
        return wagePayed;
    }

    public float getWagePerBlock() {
        return wagePerBlock;
    }
}
