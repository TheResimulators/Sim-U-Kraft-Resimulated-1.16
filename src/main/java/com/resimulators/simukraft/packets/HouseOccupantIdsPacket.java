package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.client.gui.GuiResidential;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class HouseOccupantIdsPacket implements IMessage {

    ArrayList<Integer> ids = new ArrayList<>();

    public HouseOccupantIdsPacket() {
    }

    public HouseOccupantIdsPacket(ArrayList<Integer> ids) {
        this.ids = ids;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(ids.size());
        for (int id : ids) {
            buf.writeInt(id);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ids.add(buf.readInt());
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
