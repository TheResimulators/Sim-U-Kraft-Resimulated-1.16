package com.resimulators.simukraft.common.events.world;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.init.ModEntities;
import com.resimulators.simukraft.packets.UpdateSimPacket;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class NewDayEvent implements INBTSerializable<CompoundNBT> {
    private static DayOfWeek dayOfWeek;
    private static Random random = new Random();
    private double day = 0;
    private double previousDay = 0;

    @SubscribeEvent
    public void OnNewDayEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!event.world.isRemote){
            World world = event.world;
            double time = world.getDayTime();
            day = Math.floor(time / 24000);
            dayOfWeek = DayOfWeek.of((int) (1 + (day % 7)));

            if (day != previousDay) {

                payRent(world);
                spawnSims(world);
                previousDay = day;
                }
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


    public void payRent(World world) {
        SimuKraft.LOGGER().debug("Paying rent...");
        SavedWorldData worldData = SavedWorldData.get(world);
        ArrayList<Faction> factions = worldData.getFactions();
        for (Faction faction: factions){
            float rent = faction.getRent();
            faction.sendFactionChatMessage(rent +" Collected in rent today",world);
            faction.addCredits(rent);

        }
    }

    public static DayOfWeek getDay() {
        return dayOfWeek;
    }

    public void spawnSims(World world) {

        SavedWorldData worldData = SavedWorldData.get(world);

        ArrayList<Faction> factions = worldData.getFactions();
        ServerWorld sWorld = (ServerWorld) world;


        if (sWorld.getPlayers().size() == 0) return;
        for (Faction faction : factions) {
            if (faction.getUnemployedSims().isEmpty() || true) { // temporary for testing and until residential system is done
                ArrayList<SimEntity> simsToSpawn = new ArrayList<>();
                simsToSpawn.add(new SimEntity(ModEntities.ENTITY_SIM, world));
                for (SimEntity sim : simsToSpawn) {
                    UUID id = faction.getPlayers().get(random.nextInt(faction.getPlayers().size()));
                    PlayerEntity player = world.getPlayerByUuid(id);
                    if (player != null) {
                        //gets blocks around player to spawn sim at
                        ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundPosition(player.getPosition(), 10);
                        if (spawn(sWorld, sim, blocks)) {
                            worldData.addSimToFaction(faction.getId(), sim);
                            faction.sendPacketToFaction(new UpdateSimPacket(sim.getUniqueID(), faction.getSimInfo(sim.getUniqueID()), faction.getId()));
                        }
                    }
                }
            } else {
                SimuKraft.LOGGER().debug("There are unemployed sims");
            }
        }
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
    private boolean spawn(ServerWorld world, SimEntity sim, ArrayList<BlockPos> blocksAroundPlayer) {
        // loop through provided blocks
        SimuKraft.LOGGER().debug(blocksAroundPlayer.size());

            BlockPos spawnPos = null;
            // if there are no invalid positions, spawn sim and break out of loop
            for (int i = 0;i< blocksAroundPlayer.size(); i++){
                int index = random.nextInt(blocksAroundPlayer.size()-i);
                spawnPos = getSpawnPosition(world, blocksAroundPlayer.get(index), sim);
                if (spawnPos == null){
                    SimuKraft.LOGGER().debug("AHHHH NULL " + index);
                    blocksAroundPlayer.remove(i);
                }else{
                    break;
                }

            }

            if (spawnPos != null){
            // get x, y, z coords of block
            double x = spawnPos.getX();
            double y = spawnPos.getY();
            double z = spawnPos.getZ();
            // set the sim's position
            sim.setPosition(x, y, z);
            world.addEntity(sim);// spawn entity
            sim.onInitialSpawn(world, world.getDifficultyForLocation(spawnPos), SpawnReason.TRIGGERED, null, null);
            SimuKraft.LOGGER().debug("entity spawned");
            return true;
        }
        SimuKraft.LOGGER().debug("ERROR! CANNOT SPAWN SIM.");
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
     * @return A valid position or null if none
     */
    private BlockPos getSpawnPosition(World world, BlockPos startingPos, SimEntity sim) {
        BlockPos groundBlockPos = BlockUtils.getGroundBlock(world, startingPos);
        SimuKraft.LOGGER().debug("ground block pos: " + groundBlockPos);
        // If the ground block is not valid, can't use it
        if (!BlockUtils.blockIsValid(world, groundBlockPos, sim)) {
            SimuKraft.LOGGER().debug("Block is NOT valid");
            return null;
        }
        ArrayList<BlockPos> blocksAroundGroundBlock = BlockUtils.getBlocksAroundPosition(groundBlockPos, 1);
        // If the ground block is valid and the blocks around it are valid, return the block above
        if (BlockUtils.blocksAreValid(world, blocksAroundGroundBlock, sim)) {
            SimuKraft.LOGGER().debug("Block is valid");
            return groundBlockPos.up();
        }
        SimuKraft.LOGGER().debug("Blocks are NOT valid");
        return null;
    }
}
