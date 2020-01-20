package com.Resimulators.simukraft.common.events.world;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModEntities;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jline.utils.Log;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class NewDayEvent implements INBTSerializable<CompoundNBT> {
    private static DayOfWeek dayOfWeek;
    private static Random random = new Random();
    private double day = 0;
    private double previousDay = 0;
    private int radius = 10;

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
        ArrayList<Faction> factions = worldData.getFactions();
        for (Faction faction : factions) {
            if (faction.getUnemployedSims().isEmpty()) {
                EntitySim sim = new EntitySim(ModEntities.ENTITY_SIM, world);
                SavedWorldData.get(world).addSimToFaction(faction.getId(),sim);
                //faction.addsim(sim);
                ArrayList<UUID> players = faction.getPlayers();
                ServerWorld sWorld = (ServerWorld) world;
                if (sWorld.getPlayers().size() > 0){
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
        }}
    }

    public static DayOfWeek getDay() {
        return dayOfWeek;
                PlayerEntity player = sWorld.getPlayerByUuid(players.get(random.nextInt(players.size())));
                do {
                    ArrayList<BlockPos> blocks = Lists.newArrayList(getBlocksAroundPlayer(player.getPosition(), radius));
                    Log.info(radius);
                    if (spawn(sWorld, sim, blocks)) {
                        break;
                    }
                    radius += 5;
                } while (radius <= 50);
                radius = 10;
            } else {
                Log.info("There are unemployed sims");
            }
        }
    }

    private ArrayList<BlockPos> getBlocksAroundPlayer(BlockPos pos, int radius) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Iterable<BlockPos> blocks = BlockPos.getAllInBoxMutable(x - radius, y, z - radius, x + radius, y, z + radius);
        return Lists.newArrayList(blocks);
    }

    /**
     * Loops through the blocks to try
     * and find a suitable spawning point
     *
     * @param world  The world
     * @param sim    The sim
     * @param blocks The blocks around the player
     * @return Whether or not the sim spawned
     */
    private boolean spawn(ServerWorld world, EntitySim sim, ArrayList<BlockPos> blocks) {
        Log.info("Blocks size: " + blocks.size());
        // loop through provided blocks
        for (int i = 0; i < blocks.size(); i++) {
            final BlockPos blockPos = blocks.get(i);

            // get x,y,z coords of block
            double x = blockPos.getX();
            double y = blockPos.getY();
            double z = blockPos.getZ();
            // if there are no invalid positions, spawn sim and break out of loop
            if (!canSpawn(world, blockPos).isPresent()) {
                // set the sim's position
                sim.setPosition(x, y, z);
                world.func_217460_e(sim); // spawn entity
                return true;
            }
        }
        // error, no valid position
        return false;
    }

    /**
     * Can the sim spawn in an open area?
     * A A A
     * A S A
     * A A A
     *
     * @param world     The world
     * @param entityPos The entities's position
     * @return The first invalid position, if any
     */
    private Optional<BlockPos> canSpawn(World world, BlockPos entityPos) {
        // iterate from -1,0,-1 to 1,3,1 around entity
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    final BlockPos pos = entityPos.add(x, y, z);
                    BlockState standingBlockState;
                    int attempts = 0;
                    do {
                        standingBlockState = world.getBlockState(pos.down().subtract(new Vec3i(x, y - attempts, z)));
                        if (standingBlockState.isSolid()) break;
                        attempts++;
                    } while (attempts < 10);
                    final Block block = world.getBlockState(pos).getBlock();

                    if (block != Blocks.AIR) {
                        return Optional.of(pos);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static DayOfWeek getDay() {
        return dayOfWeek;
    }
}
