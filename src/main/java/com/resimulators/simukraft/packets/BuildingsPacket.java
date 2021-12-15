package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.client.gui.GuiBuilder;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildingsPacket implements IMessage {
    private ArrayList<BuildingTemplate> templates;

    public BuildingsPacket() {
    }

    public BuildingsPacket(ArrayList<BuildingTemplate> templates) {
        this.templates = templates;

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(templates.size());
        for (BuildingTemplate template : templates) {
            buf.writeUtf(template.getName());
            buf.writeInt(template.getTypeID());
            buf.writeFloat(template.getCost());
            buf.writeFloat(template.getRent());
            buf.writeUtf(template.getAuthor());
            HashMap<Item,Integer> items = template.getBlockList();
            buf.writeInt(items.size());
            for (Item item: items.keySet()){
                buf.writeInt(Item.getId(item));
                buf.writeInt(items.get(item));
            }
        }

    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        templates = new ArrayList<>();
        int size = buf.readInt();
       for (int i = 0; i < size; i++) {
            BuildingTemplate template = new BuildingTemplate();
            template.setName(buf.readUtf());
            template.setTypeID(buf.readInt());
            template.setCost(buf.readFloat());
            template.setRent(buf.readFloat());
            template.setAuthor(buf.readUtf());
            int itemsSize = buf.readInt();
            HashMap<Item,Integer> items = new HashMap<>();
            for (int x = 0;x < itemsSize; x++){
                Item item = Item.byId(buf.readInt());
                int amount = buf.readInt();
                items.put(item,amount);
            }
            template.setBlockList(items);
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
        if (Minecraft.getInstance().screen instanceof GuiBuilder) {
            GuiBuilder gui = (GuiBuilder) Minecraft.getInstance().screen;
            gui.setStructures(templates);
        }
    }
}
