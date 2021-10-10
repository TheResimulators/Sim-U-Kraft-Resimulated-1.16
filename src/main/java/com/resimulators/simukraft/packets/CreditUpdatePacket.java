package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

public class CreditUpdatePacket implements IMessage {
    private double credit;
    private int factionId;

    public CreditUpdatePacket() {
    }

    public CreditUpdatePacket(double credits, int factionId) {
        this.credit = credits;
        this.factionId = factionId;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeDouble(credit);
        buf.writeInt(factionId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        credit = buf.readDouble();
        factionId = buf.readInt();
    }


    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        World world = SimuKraft.proxy.getClientWorld();
        Faction faction = SavedWorldData.get(world).getFaction(factionId);
        if (faction != null) {
            faction.setCredits(credit);
        }


    }
}
