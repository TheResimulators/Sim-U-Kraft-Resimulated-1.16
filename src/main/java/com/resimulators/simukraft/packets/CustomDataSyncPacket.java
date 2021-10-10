package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

public class CustomDataSyncPacket implements IMessage {

    private float price;
    private float rent;
    private BuildingType type;
    private BlockPos pos;


    public CustomDataSyncPacket() {
    }

    public CustomDataSyncPacket(float price, float rent, BuildingType type, BlockPos pos) {
        this.price = price;
        this.rent = rent;
        this.type = type;
        this.pos = pos;

    }


    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeFloat(price);
        buf.writeFloat(rent);
        buf.writeInt(type.id);
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        price = buf.readFloat();
        rent = buf.readFloat();
        type = BuildingType.getById(buf.readInt());
        pos = buf.readBlockPos();
    }

    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ctxIn.getSender() != null) {
            World world = ctxIn.getSender().level;
            if (world != null) {
                TileCustomData tile = (TileCustomData) world.getBlockEntity(pos);
                if (tile != null) {
                    tile.setPrice(price);
                    tile.setRent(rent);
                    tile.setBuildingType(type);
                }

            }
        }

    }
}
