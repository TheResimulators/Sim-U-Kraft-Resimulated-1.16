package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimFirePacket implements IMessage {
    private int factionId;
    private UUID simId;
    private BlockPos pos;
    private boolean dying;

    public SimFirePacket() {
    }

    public SimFirePacket(int factionId, UUID simId, BlockPos pos, boolean dying) {
        this.pos = pos;
        this.factionId = factionId;
        this.simId = simId;
        this.dying = dying;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(factionId);
        buf.writeUUID(simId);
        buf.writeBoolean(dying);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.factionId = buf.readInt();
        this.simId = buf.readUUID();
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
            if (world.getBlockEntity(pos) != null)
            {
            ((ITile) world.getBlockEntity(pos)).fireSim();
            }
            SavedWorldData.get(world).getFaction(factionId).fireSim(simId);

        }
        }
    }

