package com.resimulators.simukraft.common.events.world;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.SimFirePacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class SimDeathEvent {

    @SubscribeEvent
    public void OnSimDeathEvent(LivingDeathEvent event){
        if (!event.getEntity().world.isRemote) {
            LivingEntity entity = event.getEntityLiving();
            World world = entity.getEntityWorld();
            if (entity instanceof SimEntity) {
                SimEntity sim = (SimEntity) entity;

                int id = SavedWorldData.get(sim.world).getFactionWithSim(sim.getUniqueID()).getId();
                sim.fireSim(sim,id,true);
            }
        }
    }
}
