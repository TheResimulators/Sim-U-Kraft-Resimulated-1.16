package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.tileentity.TileResidential;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class RequestHouseOccupantsPacket implements IMessage {

    private BlockPos pos;
    private UUID playerId;


    public RequestHouseOccupantsPacket() {
    }

    public RequestHouseOccupantsPacket(BlockPos pos, UUID playerId) {
        this.pos = pos;
        this.playerId = playerId;

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(playerId);
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        playerId = buf.readUUID();
        pos = buf.readBlockPos();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        ServerPlayerEntity playerEntity = ctxIn.getSender();
        TileResidential tile = (TileResidential) ctxIn.getSender().level.getBlockEntity(pos);
        tile.sendOccupantsIds(playerEntity);
    }
}
