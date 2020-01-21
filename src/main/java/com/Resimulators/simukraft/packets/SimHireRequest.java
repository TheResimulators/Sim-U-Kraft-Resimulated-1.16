package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimHireRequest implements IMessage {

    private UUID playerId;
    private int simId;
    public SimHireRequest(){}

    public SimHireRequest(int simId,UUID playerId){

        this.playerId = playerId;
        this.simId = simId;
    }
    @Override
    public void toBytes(PacketBuffer buf) {

        buf.writeInt(simId);
        buf.writeUniqueId(playerId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {

        this.simId = buf.readInt();
        this.playerId = buf.readUniqueId();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId) != null){
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId);
            SavedWorldData data = SavedWorldData.get(player.world);
            int id = data.getFactionWithPlayer(player.getUniqueID()).getId();
            data.hireSim(id,(EntitySim) player.world.getEntityByID(simId));
            data.getFaction(id).sendPacketToFaction(new SimHirePacket(id,simId));
        }

    }
}
