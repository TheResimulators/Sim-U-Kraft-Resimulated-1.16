package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileFisher extends TileEntity implements IControlBlock {
    private boolean hired;
    private UUID simId;


    public TileFisher() {
        super(ModTileEntities.FISHER_MAN.get());
    }

    @Override
    public CompoundNBT getUpdateTag() { return write(new CompoundNBT());}

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putBoolean("hired", this.hired);
        if (simId != null) {
            nbt.putUniqueId("simid", simId);
        }
        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUniqueId("simid");
        }
    }

    @Override
    public int getGui() { return GuiHandler.FISHER_MAN; }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        markDirty();
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
        return "Fisher Man";
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(this.getBlockState(), pkt.getNbtCompound());
    }
}
