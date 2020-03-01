package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
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
        buf.writeUniqueId(simId);
        buf.writeUniqueId(playerId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.simId = buf.readUniqueId();
        this.playerId = buf.readUniqueId();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId) != null) {
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId);
            SavedWorldData data = SavedWorldData.get(player.world);
            int id = data.getFactionWithPlayer(player.getUniqueID()).getId();
            ((ITile)player.world.getTileEntity(pos)).setHired(false);
            System.out.println(player.world.getTileEntity(pos));
            data.fireSim(id, (EntitySim) ((ServerWorld)player.world).getEntityByUuid(simId));
            ((ITile) player.world.getTileEntity(pos)).setSimId(null);
            EntitySim sim = (EntitySim) ((ServerWorld)player.world).getEntityByUuid(simId);
            int simid =sim.getEntityId();
            sim.getJob().removeJobAi();
            sim.setJob(null);


            data.getFaction(id).sendPacketToFaction(new SimFirePacket(id, simid, pos));

        }
    }
}
