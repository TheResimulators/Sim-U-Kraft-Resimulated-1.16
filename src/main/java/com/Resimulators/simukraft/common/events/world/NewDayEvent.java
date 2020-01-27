package com.Resimulators.simukraft.common.events.world;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModEntities;
import com.Resimulators.simukraft.packets.UpdateSimPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
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
import java.util.Random;

public class NewDayEvent implements INBTSerializable<CompoundNBT> {
    private static DayOfWeek dayOfWeek;
    private static Random random = new Random();
    private double day = 0;
    private double previousDay = 0;
    private int radius = 10;
    private static final int maxRadius = 50;

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

        ArrayList<Faction> factions = worldData.getFactions();
        ServerWorld sWorld = (ServerWorld) world;

        for (Faction faction : factions) {
            if (faction.getUnemployedSims().isEmpty() || true) {
                ArrayList<EntitySim> simsToSpawn = new ArrayList<>();
                simsToSpawn.add(new EntitySim(ModEntities.ENTITY_SIM, world));
                simsToSpawn.add(new EntitySim(ModEntities.ENTITY_SIM, world));
                for (EntitySim sim : simsToSpawn) {
                    worldData.addSimToFaction(faction.getId(), sim);
                    faction.sendPacketToFaction(new UpdateSimPacket(sim.getUniqueID(),faction.getSimInfo(sim.getUniqueID()),faction.getId()));
                    ArrayList<UUID> players = faction.getPlayers();
                    if (sWorld.getPlayers().size() > 0) {
                        PlayerEntity player = sWorld.getPlayers().get(random.nextInt(sWorld.getPlayers().size()));
                        do {
                            ArrayList<BlockPos> blocks = getBlocksAroundPosition(player.getPosition(), radius);
                            Log.info("Radius: " + radius);
                            if (spawn(sWorld, sim, blocks)) {
                                worldData.addSimToFaction(faction.getId(), sim);
                                faction.sendPacketToFaction(new UpdateSimPacket(sim.getUniqueID(), faction.getSimInfo(sim.getUniqueID()), faction.getId()));
                                break;
                            }
                            radius += 5;
                        } while (radius <= maxRadius);
                        radius = 10;
                    }
                }
            } else {
                Log.info("There are unemployed sims");
            }
        }
    }

    public static DayOfWeek getDay() {
        return dayOfWeek;
    }

    private ArrayList<BlockPos> getBlocksAroundPosition(BlockPos pos, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos blockPos = pos.add(x, 0, z);
                blocks.add(blockPos);
            }
        }
        return blocks;
    }

    /**
     * Loops through the blocks to try
     * and find a suitable spawning point
     *
     * @param world              The world
     * @param sim                The sim
     * @param blocksAroundPlayer The blocks around the player
     * @return Whether or not the sim spawned
     */
    private boolean spawn(ServerWorld world, EntitySim sim, ArrayList<BlockPos> blocksAroundPlayer) {
        Log.info("Blocks size: " + blocksAroundPlayer.size());
        // loop through provided blocks
        for (BlockPos blockPos : blocksAroundPlayer) {
            // if there are no invalid positions, spawn sim and break out of loop
            BlockPos spawnPos = getSpawnPosition(world, blockPos);
            if (spawnPos == null) continue;
            // get x,y,z coords of block
            double x = spawnPos.getX();
            double y = spawnPos.getY();
            double z = spawnPos.getZ();
            // set the sim's position
            sim.setPosition(x, y, z);
            if (sim.isNotColliding(world)) {
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
     * @param world       The world
     * @param startingPos The starting position
     * @return The first invalid position, if any
     */
    private BlockPos getSpawnPosition(World world, BlockPos startingPos) {
        BlockPos groundBlockPos = getGroundBlock(world, startingPos);
        ArrayList<BlockPos> blocksAroundGroundBlock = getBlocksAroundPosition(groundBlockPos, 1);
        if (blocksAreValid(world, blocksAroundGroundBlock)) {
            return groundBlockPos.up();
        }
        return null;
    }

    private BlockPos getGroundBlock(World world, BlockPos startingPos) {
        BlockState state = world.getBlockState(startingPos);
        if (state.isSolid()) return startingPos;
        BlockPos newPos = startingPos;
        do {
            newPos = newPos.down();
            state = world.getBlockState(newPos);
        } while (state.getBlock() != Blocks.AIR || !state.isSolid());
        return newPos;
    }

    private boolean blocksAreValid(World world, ArrayList<BlockPos> blocksPos) {
        for (BlockPos block : blocksPos)
            if (!world.getBlockState(block).getBlock().canCreatureSpawn(world.getBlockState(block), world, block, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, null))
                return false;
        return true;
    }
}
