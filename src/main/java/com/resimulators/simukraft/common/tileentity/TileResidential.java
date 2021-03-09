package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.UUID;

public class TileResidential extends TileEntity implements IControlBlock {
    private int factionID;
    private UUID houseID;
    public TileResidential() {
        super(ModTileEntities.RESIDENTIAL.get());
    }

    @Override
    public int getGui() {
        return GuiHandler.RESIDENTIAL;
    }

    @Override
    public void setHired(boolean hired) {

    }

    @Override
    public boolean getHired() {
        return false;
    }

    @Override
    public UUID getSimId() {
        return null;
    }

    @Override
    public void setSimId(UUID id) {

    }

    @Override
    public String getName() {
        return "Residential";
    }

    public int getFactionID() {
        return factionID;
    }

    public void setFactionID(int factionID) {
        this.factionID = factionID;
        markDirty();
    }

    public UUID getHouseID() {
        return houseID;
    }

    public void setHouseID(UUID houseID) {
        this.houseID = houseID;
        markDirty();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(this.getBlockState(),pkt.getNbtCompound());
    }
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (houseID != null){
            compound.putUniqueId("house id",houseID);
            compound.putInt("faction id",factionID);

        }
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        if (nbt.contains("house id")){
            houseID = nbt.getUniqueId("house id");
            factionID = nbt.getInt("faction id");
        }
        super.read(state, nbt);
    }
}
