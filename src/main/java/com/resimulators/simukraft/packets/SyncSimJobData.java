package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.init.ModJobs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class SyncSimJobData implements IMessage {

    private ArrayList<Integer> simIDs = new ArrayList<>();
    private ArrayList<Integer> jobTypes = new ArrayList<>();
    private ArrayList<CompoundNBT> nbts = new ArrayList<>();


    public SyncSimJobData() {
    }

    public SyncSimJobData(ArrayList<Integer> simID, ArrayList<Integer> jobTypes, ArrayList<ListNBT> nbts) {
        this.simIDs = simID;
        this.nbts = new ArrayList<>();
        for (ListNBT list : nbts) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("nbt", list);
            this.nbts.add(nbt);
        }
        this.jobTypes = jobTypes;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.simIDs.size());
        for (Integer id : simIDs) {
            buf.writeInt(id);
        }

        for (int id : jobTypes) {
            buf.writeInt(id);
        }

        for (CompoundNBT nbt : nbts) {
            buf.writeNbt(nbt);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            simIDs.add(buf.readInt());
        }

        for (int i = 0; i < size; i++) {
            jobTypes.add(buf.readInt());

        }
        for (int i = 0; i < size; i++) {
            nbts.add(buf.readNbt());
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        World world = SimuKraft.proxy.getClientWorld();
        for (int i = 0; i < simIDs.size(); i++) {
            int simID = simIDs.get(i);
            int jobType = jobTypes.get(i);
            CompoundNBT nbt = nbts.get(i);
            SimEntity sim = (SimEntity) world.getEntity(simID);
            if (sim.getJob() == null) {
                ModJobs.JOB_LOOKUP.get(jobType).apply(sim);
                sim.getJob().readFromNbt(nbt.getList("nbt", Constants.NBT.TAG_COMPOUND));
            }
        }
    }
}
