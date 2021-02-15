package com.resimulators.simukraft.packets;

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

public class SimHirePacket implements IMessage{
    private int factionId;
    private int simId;
    private BlockPos pos;
    private int job;
    public SimHirePacket(){}

    public SimHirePacket(int simId, int factionId, BlockPos pos,int job){
        this.pos = pos;
        this.factionId = factionId;
        this.simId = simId;
        this.job = job;
    }
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(factionId);
        buf.writeInt(simId);
        buf.writeInt(job);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.factionId = buf.readInt();
        this.simId = buf.readInt();
        this.job = buf.readInt();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        SimEntity sim = (SimEntity) Minecraft.getInstance().world.getEntityByID(simId);
        SavedWorldData.get(Minecraft.getInstance().player.world).getFaction(factionId).hireSim(sim.getUniqueID());
        ((ITile)Minecraft.getInstance().world.getTileEntity(pos)).setHired(true);
        ((ITile)Minecraft.getInstance().world.getTileEntity(pos)).setSimId(sim.getUniqueID());
        sim.setJob(ModJobs.JOB_LOOKUP.get(job).apply(sim));
        sim.getJob().setWorkSpace(pos);
    }
}
