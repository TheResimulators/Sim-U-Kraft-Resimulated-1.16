package com.resimulators.simukraft.init;


import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.SyncFactionData;
import com.resimulators.simukraft.packets.SyncSimJobData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class FactionEvents {

    @SubscribeEvent
    public void PlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event){

        if (event.getPlayer() != null){
            if(!event.getPlayer().level.isClientSide){
                World world = event.getPlayer().level;
                SavedWorldData data = SavedWorldData.get(world);
                Faction faction = data.getFactionWithPlayer(event.getPlayer().getUUID());
                if (faction == null){
                    faction = data.createNewFaction();
                    data.addPlayerToFaction(faction.getId(),event.getPlayer());
                }
                Network.getNetwork().sendToPlayer(new SyncFactionData(faction.write(new CompoundNBT()),faction.getId()),((ServerPlayerEntity) event.getPlayer()));
            }
        }
    }



}

