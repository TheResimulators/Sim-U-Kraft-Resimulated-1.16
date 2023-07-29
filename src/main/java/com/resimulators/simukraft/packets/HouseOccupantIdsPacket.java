package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiResidential;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class HouseOccupantIdsPacket implements IMessage {

    ArrayList<SimEntity> ids = new ArrayList<>();

    public HouseOccupantIdsPacket() {
    }

    public HouseOccupantIdsPacket(ArrayList<SimEntity> ids) {
        this.ids = ids;
    }

    @Override
    public void toBytes(PacketBuffer buf) { 
        buf.writeInt(ids.size());
        for (SimEntity id : ids) {
            buf.writeNbt(id.getGuiInfo(new CompoundNBT()));
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            SimEntity entity = new SimEntity(SimuKraft.proxy.getClientWorld());
            entity.readGuiInfo(buf.readNbt());
            ids.add(entity);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (Minecraft.getInstance().screen instanceof GuiResidential) {
            ((GuiResidential) Minecraft.getInstance().screen).setOccupants(ids);
        }
    }
}
