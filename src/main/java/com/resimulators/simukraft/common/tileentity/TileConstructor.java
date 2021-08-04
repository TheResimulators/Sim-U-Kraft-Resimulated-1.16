package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.building.CustomTemplateManager;
import com.resimulators.simukraft.init.ModTileEntities;
import com.resimulators.simukraft.packets.BuildingsPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.UUID;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    private UUID simId;

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
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putBoolean("hired", hired);
        if (simId != null) {
            nbt.putUUID("simid", simId);
        }
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

}
