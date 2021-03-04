package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.resimulators.simukraft.init.ModJobs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public class SyncSimJobData implements IMessage{

    private int simID;
    private int jobType;
    private CompoundNBT nbt;


    public SyncSimJobData(){}

    public SyncSimJobData(int simID, int jobType, ListNBT nbt){
        this.simID = simID;
        this.jobType = jobType;
        this.nbt = new CompoundNBT();
        this.nbt.put("nbt",nbt);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(simID);
        buf.writeInt(jobType);
        buf.writeCompoundTag(nbt);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        simID = buf.readInt();
        jobType = buf.readInt();
        nbt = buf.readCompoundTag();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        World world = SimuKraft.proxy.getClientWorld();
        SimEntity sim = (SimEntity) world.getEntityByID(simID);
        if (sim.getJob() == null) {
            ModJobs.JOB_LOOKUP.get(jobType).apply(sim);
            sim.getJob().readFromNbt(nbt.getList("nbt", Constants.NBT.TAG_COMPOUND));
        }
    }
}
