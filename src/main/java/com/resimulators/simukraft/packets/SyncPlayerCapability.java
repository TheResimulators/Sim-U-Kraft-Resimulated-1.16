package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import javax.annotation.Nullable;

public class SyncPlayerCapability implements IMessage {
    private CompoundNBT nbt;
    private int id;

    public SyncPlayerCapability(CompoundNBT nbt, int id){

        this.nbt = nbt;
        this.id = id;
    }
    public SyncPlayerCapability(){}


    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeCompoundTag(nbt);
        buf.writeInt(id);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.nbt = buf.readCompoundTag();
        id = buf.readInt();

    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return null;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        Faction faction = new Faction(id);
        faction.read(nbt);
        SavedWorldData.get(Minecraft.getInstance().world).setFaction(id,faction);
    }
}
