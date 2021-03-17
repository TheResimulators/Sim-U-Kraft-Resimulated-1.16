package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import com.resimulators.simukraft.packets.BuildingsPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    private UUID simId;

    public TileConstructor() {
        super(ModTileEntities.CONSTRUCTOR.get());
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
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state,nbt);
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUUID("simid");
        }
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        setChanged();
    }

    @Override
    public boolean getHired() {
        return hired;
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
        load(this.getBlockState(),pkt.getTag());
    }


    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    public void FindAndLoadBuilding(PlayerEntity playerEntity){
        List<ResourceLocation> locations = StructureHandler.getTemplateManager().getAllTemplates();
        ArrayList<BuildingTemplate> templates = new ArrayList<>();
        for (ResourceLocation location : locations){
            String name = location.getPath().replace(".nbt","");
            BuildingTemplate template = StructureHandler.loadStructure(name);
            if (template != null){
                templates.add(template);

            }else{
                SimuKraft.LOGGER().warn("Structure with name " + name + " is missing or corrupted and could not be loaded," +
                    " please check if it is in the right location and that it is a valid structure");

            }

        }
        Network.getNetwork().sendToPlayer(new BuildingsPacket(templates),(ServerPlayerEntity) playerEntity);
    }
}
