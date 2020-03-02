package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class UpdateSimPacket implements IMessage {
    private UUID uuid;
    private int id;
    private CompoundNBT nbt;
    public UpdateSimPacket(UUID uuid, CompoundNBT nbt,int id){
        this.uuid = uuid;
        this.nbt = nbt;
        this.id = id;
    }
    public UpdateSimPacket(){}
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        buf.writeCompoundTag(nbt);
        buf.writeInt(id);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        nbt = buf.readCompoundTag();
        id = buf.readInt();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        SavedWorldData data = SavedWorldData.get(Minecraft.getInstance().world);
        data.getFaction(id).addSim(uuid);
        data.getFaction(id).setSimInfo(uuid,nbt);
    }
}
