package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.init.ModTileEntities;
import com.resimulators.simukraft.packets.HouseOccupantIdsPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;
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
    public boolean getHired() {
        return false;
    }

    @Override
    public void setHired(boolean hired) {

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
        setChanged();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        if (nbt.contains("house id")) {
            houseID = nbt.getUUID("house id");
            factionID = nbt.getInt("faction id");
        }
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if (houseID != null) {
            compound.putUUID("house id", houseID);
            compound.putInt("faction id", factionID);

        }
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    public void sendOccupantsIds(ServerPlayerEntity player) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (factionID != 0){
            Faction faction = SavedWorldData.get(this.getLevel()).getFaction(factionID);
            ArrayList<UUID> occupants = faction.getOccupants(getHouseID());
            SimuKraft.LOGGER().info("Faction " + faction);
            for (UUID uuid : occupants) {
                ids.add(((ServerWorld) level).getEntity(uuid).getId());
            }
            SimUKraftPacketHandler.INSTANCE.sendTo(new HouseOccupantIdsPacket(ids), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

        }
    }

    public UUID getHouseID() {
        return houseID;
    }

    public void setHouseID(UUID houseID) {
        this.houseID = houseID;
        setChanged();
    }

    public void onDestroy(World world) {
        if (factionID != 0){
            SavedWorldData.get(world).getFaction(factionID).removeHouse(houseID, (ServerWorld) world);
        }
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
