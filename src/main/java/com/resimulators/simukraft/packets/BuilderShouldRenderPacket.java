package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.block.BlockControlBlock;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.core.jmx.Server;

import javax.annotation.Nullable;

public class BuilderShouldRenderPacket implements IMessage {

    private BlockPos pos;
    private boolean shouldRender;

    public BuilderShouldRenderPacket(){}
    public BuilderShouldRenderPacket(BlockPos pos,boolean shouldRender){
        this.pos = pos;
        this.shouldRender = shouldRender;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(shouldRender);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        pos = buf.readBlockPos();
        shouldRender = buf.readBoolean();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return null;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        ServerWorld world = (ServerWorld)ctxIn.getSender().level;
        TileConstructor constructor = (TileConstructor) world.getBlockEntity(pos);
        constructor.setShouldRender(shouldRender);
    }
}
