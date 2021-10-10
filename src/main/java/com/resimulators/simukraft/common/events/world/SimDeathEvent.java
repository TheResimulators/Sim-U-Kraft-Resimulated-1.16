package com.resimulators.simukraft.common.events.world;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class SimDeathEvent {

    @SubscribeEvent
    public void OnSimDeathEvent(LivingDeathEvent event) {
        if (!event.getEntity().level.isClientSide) {
            LivingEntity entity = event.getEntityLiving();
            if (entity instanceof SimEntity) {
                SimEntity sim = (SimEntity) entity;
                Faction faction = SavedWorldData.get(sim.level).getFactionWithSim(sim.getUUID());
                if (faction != null) {
                    if (sim.getHouseID() != null) {
                        sim.removeFromHouse(faction);
                    }
                    sim.fireSim(sim, faction.getId(), true);
                }
            }
        }
    }
}
