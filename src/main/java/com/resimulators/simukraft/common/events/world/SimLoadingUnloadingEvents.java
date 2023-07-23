package com.resimulators.simukraft.common.events.world;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.init.ModJobs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SimLoadingUnloadingEvents {



    @SubscribeEvent
    public void onEntityJoinEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof SimEntity) {

            SimEntity sim = (SimEntity) event.getEntity();
            SavedWorldData data = SavedWorldData.get(sim.getCommandSenderWorld());
            Faction faction = data.getFactionWithSim(sim.getUUID());
            if (faction != null) {
                Faction.SimInfo simInfo = faction.getSimInfo(sim.getUUID());


                if (simInfo.isUnloaded()) {
                    CompoundNBT nbt = faction.getSimInfoNbt(sim.getUUID());
                    if(nbt.contains("Job int")){
                        sim.setJob(ModJobs.JOB_LOOKUP.get(nbt.getInt("Job int")).apply(sim));
                        sim.getJob().readFromNbt(nbt.getList("Job nbt", Constants.NBT.TAG_LIST));
                    }
                    simInfo.setUnloaded(false);
                    simInfo.setJob(null);
                    faction.setFactionDirty();
                }
            }
        }
    }
    @SubscribeEvent
    public void onEntityLeavesEvent(EntityLeaveWorldEvent event)
    {

            if (event.getEntity() instanceof SimEntity) {

                if (!event.getEntity().isAlive()) {
                    SimEntity sim = (SimEntity) event.getEntity();
                    SavedWorldData data = SavedWorldData.get(sim.getCommandSenderWorld());
                    Faction faction = data.getFactionWithSim(sim.getUUID());
                    if (faction == null)return;
                    Faction.SimInfo simInfo = faction.getSimInfo(sim.getUUID());
                    simInfo.setUnloaded(true);
                    simInfo.setJob(sim.getJob());
                    simInfo.setSimName(sim.getCustomName().getString());
                    faction.setFactionDirty();
                }

        }
    }
}
