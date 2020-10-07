package com.resimulators.simukraft.init;


import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.SyncFactionData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FactionEvents {

    @SubscribeEvent
    public void PlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() != null){
            World world = event.getPlayer().world;
            SavedWorldData data = SavedWorldData.get(world);
            Faction faction = data.getFactionWithPlayer(event.getPlayer().getUniqueID());
            if (faction == null){
                faction = data.createNewFaction();
                data.addPlayerToFaction(faction.getId(),event.getPlayer());
            }
            Network.getNetwork().sendToPlayer(new SyncFactionData(faction.write(new CompoundNBT()),faction.getId()),((ServerPlayerEntity) event.getPlayer()));

        }
    }

}

