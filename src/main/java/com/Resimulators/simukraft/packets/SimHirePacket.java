package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public class SimHirePacket implements IMessage{
    private int factionId;
    private int simId;
    private BlockPos pos;
    public SimHirePacket(){}

    public SimHirePacket(int simId, int factionId, BlockPos pos){
        this.pos = pos;
        this.factionId = factionId;
        this.simId = simId;
    }
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(factionId);
        buf.writeInt(simId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.factionId = buf.readInt();
        this.simId = buf.readInt();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        SavedWorldData.get(Minecraft.getInstance().player.world).getFaction(factionId).hireSim(Minecraft.getInstance().world.getEntityByID(simId).getUniqueID());
        ((ITile)Minecraft.getInstance().world.getTileEntity(pos)).setHired(true);
    }
}
