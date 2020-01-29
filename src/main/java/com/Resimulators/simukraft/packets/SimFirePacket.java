package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public class SimFirePacket implements IMessage {
    private int factionId;
    private int simId;
    private BlockPos pos;
    public SimFirePacket(){}

    public SimFirePacket(int factionId,int simId,BlockPos pos){
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
        SavedWorldData.get(Minecraft.getInstance().player.world).getFaction(factionId).fireSim(Minecraft.getInstance().world.getEntityByID(simId).getUniqueID());
        ((EntitySim)Minecraft.getInstance().world.getEntityByID(simId)).setJob(null);
        ((ITile) Minecraft.getInstance().world.getTileEntity(pos)).setHired(false);
        ((ITile)Minecraft.getInstance().world.getTileEntity(pos)).setSimId(null);
    }
}
