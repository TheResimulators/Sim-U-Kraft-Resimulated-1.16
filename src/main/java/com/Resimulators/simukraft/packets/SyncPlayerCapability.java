package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.capabilities.PlayerCapability;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SyncPlayerCapability {
    private CompoundNBT nbt;
    private int id;

    public SyncPlayerCapability(CompoundNBT nbt, int id){

        this.nbt = nbt;
        this.id = id;
    }

    public static void encode(SyncPlayerCapability pkt,PacketBuffer buffer){
        buffer.writeCompoundTag(pkt.nbt);
        buffer.writeInt(pkt.id);

    }


    public static SyncPlayerCapability decode(PacketBuffer buffer){
        CompoundNBT nbt = buffer.readCompoundTag();
        int id = buffer.readInt();
        return new SyncPlayerCapability(nbt,id);

    }
    public static void handler(SyncPlayerCapability message, Supplier<NetworkEvent.Context> ctx){
        PlayerEntity player = Minecraft.getInstance().player;
        ctx.get().enqueueWork(() -> {
            LazyOptional<PlayerCapability> cap = ModCapabilities.get(player);
            Faction faction = new Faction(message.id,SavedWorldData.get(Minecraft.getInstance().world));
            faction.read(message.nbt);
            SavedWorldData.get(Minecraft.getInstance().world).setFaction(message.id,faction);
            cap.ifPresent(playerCapability -> {
                        playerCapability.setFaction(faction);
                        System.out.println(playerCapability.getFaction());
            }
            );
        });

    }
}
