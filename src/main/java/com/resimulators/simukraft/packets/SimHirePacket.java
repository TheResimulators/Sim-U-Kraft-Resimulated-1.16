package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.init.ModJobs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimHirePacket implements IMessage {
    private int factionId;
    private UUID simId;
    private BlockPos pos;
    private int job;

    public SimHirePacket() {
    }

    public SimHirePacket(UUID simId, int factionId, BlockPos pos, int job) {
        this.pos = pos;
        this.factionId = factionId;
        this.simId = simId;
        this.job = job;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(factionId);
        buf.writeUUID(simId);
        buf.writeInt(job);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.factionId = buf.readInt();
        this.simId = buf.readUUID();
        this.job = buf.readInt();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        SavedWorldData.get(SimuKraft.proxy.getClientWorld()).getFaction(factionId).hireSim(simId);
        ((ITile) Minecraft.getInstance().level.getBlockEntity(pos)).setHired(true);
        ((ITile) Minecraft.getInstance().level.getBlockEntity(pos)).setSimId(simId);
    }
}
