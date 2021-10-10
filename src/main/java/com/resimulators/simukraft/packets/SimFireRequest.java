package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimFireRequest implements IMessage {
    private UUID playerId;
    private UUID simId;
    private BlockPos pos;

    public SimFireRequest() {
    }

    public SimFireRequest(UUID playerId, UUID simId, BlockPos pos) {
        this.playerId = playerId;
        this.simId = simId;
        this.pos = pos;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeUUID(simId);
        buf.writeUUID(playerId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.simId = buf.readUUID();
        this.playerId = buf.readUUID();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId) != null) {
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                SavedWorldData data = SavedWorldData.get(player.level);
                int id = data.getFactionWithPlayer(player.getUUID()).getId();
                SimEntity sim = (SimEntity) ((ServerWorld) player.level).getEntity(simId);
                if (sim != null) {
                    sim.fireSim(sim, id, false);
                }
            }
        }
    }
}
