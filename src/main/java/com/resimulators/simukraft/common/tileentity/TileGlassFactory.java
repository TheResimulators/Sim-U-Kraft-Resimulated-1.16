package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileGlassFactory extends TileEntity implements IControlBlock {
    private boolean hired;
    private UUID simID;

    public TileGlassFactory() {
        super(ModTileEntities.GLASS_FACTORY.get());
    }

    @Override
    public int getGui() {
        return GuiHandler.GLASS_FACTORY;
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
        return simID;
    }

    @Override
    public void setSimId(UUID id) {
        this.simID = id;
    }

    @Override
    public String getName() {
        return "Glass Factory";
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simID = nbt.getUUID("simid");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putBoolean("hired", hired);
        if (simID != null) {
            nbt.putUUID("simid", simID);
        }
        return nbt;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }


    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        this.load(blockState, parentNBTTagCompound);
    }


    @Override
    public void fireSim() {
        setHired(false);
        setSimId(null);
    }
}
