package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.client.gui.GuiBaseJob;
import com.resimulators.simukraft.client.gui.GuiBuilder;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BuildingsPacket implements IMessage {
    private ArrayList<BuildingTemplate> templates;
    public BuildingsPacket(){}

    public BuildingsPacket(ArrayList<BuildingTemplate> templates){
        this.templates = templates;

    }
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(templates.size());
        for (BuildingTemplate template: templates){
            buf.writeString(template.getName());
            buf.writeInt(template.getTypeID());
            buf.writeFloat(template.getCost());
            buf.writeFloat(template.getRent());
            buf.writeString(template.getAuthor());
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        templates = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i< size;i++){
            BuildingTemplate template = new BuildingTemplate();
            template.setName(buf.readString());
            template.setTypeID(buf.readInt());
            template.setCost(buf.readFloat());
            template.setRent(buf.readFloat());
            template.setAuthor(buf.readString());
            templates.add(template);
        }
    }
    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (Minecraft.getInstance().currentScreen instanceof GuiBuilder){
            GuiBuilder gui = (GuiBuilder)Minecraft.getInstance().currentScreen;
            gui.setStructures(templates);
        }
    }
}
