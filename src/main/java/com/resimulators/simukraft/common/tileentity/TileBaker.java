package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileBaker extends TileEntity implements IControlBlock {
    private boolean hired;
    private UUID simId;


    public TileBaker() {
        super(ModTileEntities.BAKER.get());
    }

    @Override
    public CompoundNBT getUpdateTag() { return save(new CompoundNBT());}

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putBoolean("hired", this.hired);
        if (simId != null) {
            nbt.putUUID("simid", simId);
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUUID("simid");
        }
    }

    @Override
    public int getGui() { return GuiHandler.BAKER; }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        setChanged();
    }

    @Override
    public boolean getHired() {
        return this.hired;
    }

    @Override
    public UUID getSimId() {
        return this.simId;
    }

    @Override
    public void setSimId(UUID id) {
        this.simId = id;
    }

    @Override
    public String getName() {
        return "Baker";
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }
}
