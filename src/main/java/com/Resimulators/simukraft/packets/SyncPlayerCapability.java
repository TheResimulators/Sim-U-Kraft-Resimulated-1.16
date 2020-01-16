package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.capabilities.PlayerCapability;
import com.Resimulators.simukraft.common.world.Faction;
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

    public SyncPlayerCapability(CompoundNBT nbt){
        this.nbt = nbt;
    }

    public static void encode(SyncPlayerCapability pkt,PacketBuffer buffer){
        buffer.writeCompoundTag(pkt.nbt);

    }


    public static SyncPlayerCapability decode(PacketBuffer buffer){
        CompoundNBT nbt = buffer.readCompoundTag();
        return new SyncPlayerCapability(nbt);

    }
    public static void handler(SyncPlayerCapability message, Supplier<NetworkEvent.Context> ctx){
        PlayerEntity player = Minecraft.getInstance().player;
        ctx.get().enqueueWork(() -> {
            LazyOptional<PlayerCapability> cap = ModCapabilities.get(player);
            Faction faction = new Faction();
            faction.read(message.nbt);
            cap.ifPresent(playerCapability -> {
                        playerCapability.setFaction(faction);
                        System.out.println(playerCapability.getFaction());
            }
            );
        });

    }
}
