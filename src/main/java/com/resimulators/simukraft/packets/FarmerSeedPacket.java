package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

public class FarmerSeedPacket implements IMessage {
    private Seed seed;
    private BlockPos pos;

    public FarmerSeedPacket() {
    }

    public FarmerSeedPacket(Seed seed, BlockPos pos) {
        this.seed = seed;
        this.pos = pos;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(seed.getId());
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        pos = buf.readBlockPos();
        seed = Seed.getSeedById(buf.readInt());
    }


    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        ctxIn.enqueueWork(() -> {
            ServerPlayerEntity player = ctxIn.getSender();
            if (player != null) {
                TileFarmer farmer = (TileFarmer) player.level.getBlockEntity(pos);
                if (farmer != null) {
                    farmer.setSeed(seed);
                }
            }
        });
    }
}
