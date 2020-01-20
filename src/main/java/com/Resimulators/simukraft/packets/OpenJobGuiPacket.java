package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.client.gui.BaseJobGui;
import com.Resimulators.simukraft.common.capabilities.PlayerCapability;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.Supplier;

public class OpenJobGuiPacket {
    private ArrayList<Integer> ints;
    public OpenJobGuiPacket(ArrayList<Integer> ints){
    this.ints = ints;
    }

    public static void encode(OpenJobGuiPacket pkt, PacketBuffer buffer){
        buffer.writeInt(pkt.ints.size());
        for(int id:pkt.ints){
            buffer.writeInt(id);
        }
    }


    public static OpenJobGuiPacket decode(PacketBuffer buffer){
        ArrayList<Integer> ids = new ArrayList<>();
        int length = buffer.readInt();
        for(int i = 0;i<length;i++){
            ids.add(buffer.readInt());
        }
        return new OpenJobGuiPacket(ids);
    }
    public static void handler(OpenJobGuiPacket message, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().displayGuiScreen(new BaseJobGui(new StringTextComponent("Base"),message.ints));
        });
    }
}
