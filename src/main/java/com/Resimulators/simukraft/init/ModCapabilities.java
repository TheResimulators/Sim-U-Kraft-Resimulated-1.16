package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.common.capabilities.PlayerCapability;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.Resimulators.simukraft.packets.SyncPlayerCapability;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;

public class ModCapabilities {


    public static LazyOptional<PlayerCapability> get(LivingEntity player) {
        return player.getCapability(PlayerCapability.Provider.TEST);
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(PlayerCapability.class, new PlayerCapability.Storage() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<PlayerCapability> capability, PlayerCapability instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<PlayerCapability> capability, PlayerCapability instance, Direction side, INBT nbt) {
                instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, () -> {
            throw new RuntimeException("Can not instantiate this way. The capability needs a player as context.");
        });
        MinecraftForge.EVENT_BUS.register(new AttachHandlers());
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    static class AttachHandlers {

        AttachHandlers() {
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof PlayerEntity) {
                event.addCapability(PlayerCapability.CAPABILITY_ID, new PlayerCapability.Provider());


            }

        }

    }

    static class EventHandlers {


        @SubscribeEvent
        public void joinWorld(PlayerEvent.PlayerLoggedInEvent event) {
            ServerPlayerEntity entity = (ServerPlayerEntity) event.getEntity();
            LazyOptional<PlayerCapability> cap = ModCapabilities.get(entity);
            SavedWorldData data = SavedWorldData.get(entity.world);
            cap.ifPresent(playerCapability -> {
                playerCapability.setFaction(SavedWorldData.get(event.getPlayer().getEntityWorld()).getFactionWithPlayer(event.getEntity().getUniqueID()));
                if (playerCapability.getFaction() == null) {
                    Faction faction = data.createNewFaction();
                    playerCapability.setFaction(faction);
                    faction.addPlayer(event.getPlayer().getUniqueID());
                }else if (data.getFaction(playerCapability.getFactionId()) == null){
                    data.setFaction(playerCapability.getFaction().getId(),playerCapability.getFaction());}

            });
            if (entity.world.isRemote) return;
            syncToPlayer(entity);


        }


        public void syncToPlayer(ServerPlayerEntity entity) {

            LazyOptional<PlayerCapability> cap = ModCapabilities.get(entity);
            cap.ifPresent(playerCapability -> {
                int id = playerCapability.getFaction().getId();
                SimUKraftPacketHandler.INSTANCE.sendTo(new SyncPlayerCapability(playerCapability.serializeNBT(),id),
                    entity.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                }
            );

        }
    }
}
