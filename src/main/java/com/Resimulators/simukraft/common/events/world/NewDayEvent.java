package com.Resimulators.simukraft.common.events.world;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jline.utils.Log;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class NewDayEvent implements INBTSerializable<CompoundNBT> {
    private static DayOfWeek dayOfWeek;
    private static Random random = new Random();
    private double day = 0;
    private double previousDay = 0;
    private double x;

    @SubscribeEvent
    public void OnNewDayEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            World world = event.world;
            double time = world.getDayTime();
            day = Math.floor(time / 24000);
            dayOfWeek = DayOfWeek.of((int) (1 + (day % 7)));

            if (day != previousDay) {
                payRent();
                spawnSims(world);
                previousDay = day;
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble("day", day);
        nbt.putDouble("previousDay", previousDay);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        day = nbt.getDouble("day");
        previousDay = nbt.getDouble("previousDay");
    }


    public void payRent() {
        Log.info("Paying rent...");
    }

    public void spawnSims(World world) {
        SavedWorldData worldData = SavedWorldData.get(world);
        List<Faction> worldFactions = new ArrayList<Faction>();
        ArrayList<Integer> factionIDs = worldData.getFactionIds();
        for (Integer item : factionIDs) {
            if (worldData.getFaction(item) != null) {
                worldFactions.add(worldData.getFaction(item));
            }
        }
        for (Faction faction : worldFactions) {
            if (faction.getUnemployedSims().isEmpty()) {
                EntitySim sim = new EntitySim(ModEntities.ENTITY_SIM, world);
                faction.addsim(sim);
                ArrayList<UUID> players = faction.getPlayers();
                ServerWorld sWorld = (ServerWorld) world;
                PlayerEntity player = sWorld.getPlayers().get(random.nextInt(sWorld.getPlayers().size()));
                BlockPos pos = player.getPosition();
                double x = pos.getX();
                double y = pos.getY();
                double z = pos.getZ();
                Log.info("Position: " + pos);
//                TODO: change spawning system
                sim.setPosition(x, y + 3, z);
                sWorld.func_217460_e(sim); // spawn entity
            } else {
                Log.info("There are unemployed sims");
            }
        }
    }

    public static DayOfWeek getDay() {
        return dayOfWeek;
    }
}
