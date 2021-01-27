package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.world.Faction.House;
import com.resimulators.simukraft.common.world.SavedWorldData;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class NewHousePacket implements IMessage{
    private House house;
    private int faction;
    private UUID houseId;
    public NewHousePacket(){}

    public NewHousePacket(House house,UUID houseID, int faction){
        this.house = house;
        this.faction = faction;
        this.houseId = houseID;

    }
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(faction);
        buf.writeCompoundTag(house.write());
        buf.writeUniqueId(houseId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        faction = buf.readInt();
        house = new House();
        CompoundNBT nbt = buf.readCompoundTag();
        if (nbt != null){
        house.read(nbt);
        }
        houseId = buf.readUniqueId();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        SavedWorldData.get(SimuKraft.proxy.getClientWorld()).getFaction(faction).addHouse(house,houseId);
    }
}
