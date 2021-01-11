package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.building.BuildingTemplate;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class BuildingsPacket implements IMessage {

    public BuildingsPacket(){}

    public BuildingsPacket(ArrayList<BuildingTemplate> templates){}
    @Override
    public void toBytes(PacketBuffer buf) {

    }

    @Override
    public void fromBytes(PacketBuffer buf) {

    }
    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return null;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {

    }
}
