package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public class SimFirePacket implements IMessage {
    private int factionId;
    private int simId;
    private BlockPos pos;
    private boolean dying;

    public SimFirePacket() {
    }

    public SimFirePacket(int factionId, int simId, BlockPos pos, boolean dying) {
        this.pos = pos;
        this.factionId = factionId;
        this.simId = simId;
        this.dying = dying;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(factionId);
        buf.writeInt(simId);
        buf.writeBoolean(dying);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.factionId = buf.readInt();
        this.simId = buf.readInt();
        this.dying = buf.readBoolean();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            SimEntity sim = (SimEntity) world.getEntity(simId);
            if (sim != null) {
                sim.fireSim(sim, factionId, dying);

            }
        }
    }
}
