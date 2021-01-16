package com.resimulators.simukraft.packets;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

public class StartBuildingPacket implements IMessage {

    private BlockPos pos;
    private Direction direction;
    private String name;

    public StartBuildingPacket(){}

    public StartBuildingPacket(BlockPos pos, Direction dir,String name){
        this.pos = pos;
        direction = dir;
        this.name = name;

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(direction.getHorizontalIndex());
        buf.writeString(name);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        pos = buf.readBlockPos();
        direction
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {

    }
}
