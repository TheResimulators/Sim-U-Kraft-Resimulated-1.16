package com.resimulators.simukraft.common.world;

import com.google.common.collect.Lists;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.packets.CreditUpdatePacket;
import com.resimulators.simukraft.packets.IMessage;
import com.resimulators.simukraft.packets.NewHousePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Faction {
    private final ArrayList<UUID> players = new ArrayList<>();
    private final HashMap<UUID, SimInfo> sims = new HashMap<>();
    private final HashMap<UUID, House> houses = new HashMap<>();
    private final World world;
    private Random rnd;
    private double credits = 0d;
    private int id;

    public Faction(int id, World world) {
        this.id = id;
        this.world = world;

    }

    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (UUID uuid : players) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUUID("player", uuid);
            list.add(compound);
        }
        nbt.put("players", list);

        list = new ListNBT();
        for (UUID id : sims.keySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUUID("sim", id);
            compound.put("siminfo", sims.get(id).write());
            list.add(compound);
        }

        nbt.put("sims", list);
        list = new ListNBT();
        for (UUID uuid : houses.keySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUUID("houseID", uuid);
            compound.put("house nbt", houses.get(uuid).write());
            list.add(compound);
        }
        nbt.put("houses", list);
        nbt.putDouble("credits", credits);
        nbt.putInt("id", id);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
        for (INBT player : players) {
            CompoundNBT compound = (CompoundNBT) player;
            this.players.add(compound.getUUID("player"));
        }
        ListNBT sims = nbt.getList("sims", Constants.NBT.TAG_COMPOUND);

        for (INBT sim : sims) {
            CompoundNBT compound = (CompoundNBT) sim;
            UUID id = compound.getUUID("sim");
            SimInfo info = new SimInfo(id);
            info.read(compound.getCompound("siminfo"));
            this.sims.put(id, info);
        }
        ListNBT houses = nbt.getList("houses", Constants.NBT.TAG_COMPOUND);
        for (INBT house : houses) {
            CompoundNBT compound = (CompoundNBT) house;
            UUID id = compound.getUUID("houseID");
            House newHouse = new House();
            newHouse.read(compound.getCompound("house nbt"));
            this.houses.put(id, newHouse);

        }
        this.credits = nbt.getDouble("credits");
        this.id = id;
    }

    public void addSim(SimEntity sim) {
        validateSims((ServerWorld) sim.level);
        addSim(sim.getUUID());
    }

    public void validateSims(ServerWorld world) {
        List<UUID> temp = Lists.newArrayList();
        for (UUID id : sims.keySet()) {
            Entity entity = world.getEntity(id);
            if (entity == null)
                temp.add(id);
        }
        for (UUID id : temp) {
            sims.remove(id);
        }
    }

    public void addSim(UUID id) {
        sims.put(id, new SimInfo(id));
    }

    public void removeSim(SimEntity sim) {
        sims.remove(sim.getUUID());
    }

    public void subCredits(double credits) {
        setCredits(this.credits - credits);

    }

    private void updateCredits() {
        sendPacketToFaction(new CreditUpdatePacket(credits, this.id));
    }

    public void sendPacketToFaction(IMessage message) {
        for (UUID id : players) {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(id);
                if (player != null) {
                    Network.handler.sendToPlayer(message, player);
                }
            }
        }
    }
    public void removeSim(UUID id) {
        sims.remove(id);
    }


    public void addCredits(double credits) {
        setCredits(this.credits + credits);
    }

    public double getCredits() {
        return this.credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
        if (world != null) {
            if (!world.isClientSide) {
                updateCredits();
            }
        }
    }

    public boolean hasEnoughCredits(double creditsNeeded) {
        if (this.credits >= creditsNeeded) {
            return true;
        }

        return true; // change this to false when everything else is done
    }

    public ArrayList<UUID> getUnemployedSims() {
        ArrayList<UUID> sims = new ArrayList<>();

        for (SimInfo info : this.sims.values()) {
            if (!info.hired) {
                sims.add(info.sim);
            }


        }
        return sims;
    }

    public ArrayList<UUID> getEmployedSims() {
        ArrayList<UUID> sims = new ArrayList<>();

        for (SimInfo info : this.sims.values()) {
            if (info.hired) {
                sims.add(info.sim);
            }
        }
        return sims;
    }

    public void hireSim(SimEntity sim) {
        this.hireSim(sim.getUUID());
    }

    public void hireSim(UUID id) {
        this.sims.get(id).hired = true;
    }

    public void fireSim(SimEntity sim) {
        this.fireSim(sim.getUUID());
    }

    public void fireSim(UUID id) {
        this.sims.get(id).hired = false;
    }

    public int getAmountOfSims() {
        return sims.size();
    }

    public ArrayList<Integer> getSimIds(ServerWorld world) {
        ArrayList<Integer> simids = new ArrayList<>();
        for (UUID id : sims.keySet()) {
            Entity entity = world.getEntity(id);
            if (entity != null) {
                simids.add(entity.getId());
            } else {
                SimuKraft.LOGGER().error("Error: Entity doesn't exist in faction. Please contact the author.");
            }
        }
        return simids;
    }

    public ArrayList<Integer> getSimUnemployedIds(ServerWorld world) {
        ArrayList<Integer> simids = new ArrayList<>();
        for (UUID id : sims.keySet()) {
            Entity entity = world.getEntity(id);
            if (entity != null) {
                if (!sims.get(id).hired) {
                    simids.add(entity.getId());
                }
            } else {
                SimuKraft.LOGGER().error("Error: Unemployed entity doesn't exist in faction while trying to get Unemployed Sims. Please contact the author.");
            }
        }
        return simids;
    }

    public HashMap<UUID, SimInfo> getSimsAndInfo() {
        return sims;
    }

    public ArrayList<UUID> getSims() {
        return new ArrayList<>(sims.keySet());


    }

    public void addPlayer(UUID player) {
        this.players.add(player);
    }

    public int getId() {
        return id;
    }

    public void removeAllSims() {
        sims.keySet().iterator().forEachRemaining(sims::remove);
    }

    public void removeAllPlayers() {
        players.iterator().forEachRemaining(players::remove);
    }

    public UUID getOnlineFactionPlayer() {
        for (UUID id : players) {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(id);
                if (player != null) {
                    return id;
                }
            }

        }
        return null;
    }

    public boolean containsSim(UUID id) {
        return sims.containsKey(id);
    }

    public void setSimInfo(UUID id, CompoundNBT nbt) {
        sims.get(id).read(nbt);
    }

    public CompoundNBT getSimInfo(UUID id) {
        return sims.get(id).write();
    }

    public void sendFactionChatMessage(String string, World world) {
        for (UUID id : getPlayers()) {
            PlayerEntity entity = world.getPlayerByUUID(id);
            if (entity != null) {
                entity.sendMessage(new StringTextComponent(string), id);

            }

        }

    }

    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public boolean getHired(UUID id) {
        return sims.get(id).hired;
    }


    public UUID addNewHouse(BlockPos pos, String name, float rent) {
        UUID id = UUID.randomUUID();
        House house = new House(pos, name, rent);
        houses.put(id, house);
        sendPacketToFaction(new NewHousePacket(house, id, this.id));
        return id;
    }

    public boolean removeHouse(UUID id, ServerWorld world) {
        if (houses.get(id) == null) return false;
        for (UUID simID : houses.get(id).simOccupants) {
            SimEntity entity = (SimEntity) world.getEntity(simID);
            if (entity != null) {
                entity.removeHouse();
            }

        }
        houses.remove(id);
        return true;
    }


    public void addSimToHouse(UUID houseID, UUID simID) {
        House house = houses.get(houseID);
        sims.get(simID).homeless = false;
        house.simOccupants.add(simID);
    }

    public boolean removeSimFromHouse(UUID houseID, UUID simID) {
        House house = houses.get(houseID);
        sims.get(simID).homeless = true;
        return house.simOccupants.remove(simID);

    }

    public House getHouseByID(UUID uuid) {
        return houses.get(uuid);
    }

    public ArrayList<UUID> getOccupants(UUID houseID) {
        return houses.get(houseID).simOccupants;
    }

    public UUID getFreeHouse() {
        for (UUID house : houses.keySet()) {
            if (houses.get(house).simOccupants.isEmpty()) {
                return house;
            }

        }
        return null;
    }

    public int getFreeHousesCount() {
        int size = 0;
        for (UUID house : houses.keySet()) {
            if (houses.get(house).simOccupants.isEmpty()) {
                size++;
            }

        }
        return size;
    }

    public int getHomelessSimCount() {
        int count = 0;
        for (SimInfo info : sims.values()) {
            if (info.homeless) {
                count++;
            }
        }
        return count;
    }

    public void addHouse(House house, UUID houseID) {
        houses.put(houseID, house);

    }

    public float getRent() {
        float rent = 0;
        List<House> rentHouses = getAllOccupiedHouses();
        for (House house : rentHouses) {
            rent += house.rent;
        }
        return rent;
    }

    public ArrayList<House> getAllOccupiedHouses() {
        ArrayList<House> houses = new ArrayList<>();
        for (UUID house : this.houses.keySet()) {
            if (!this.houses.get(house).simOccupants.isEmpty()) {
                houses.add(this.houses.get(house));
            }

        }
        return houses;
    }

    static class SimInfo {
        private UUID sim;
        private boolean hired;
        private boolean homeless = true;

        SimInfo(UUID sim) {
            this.sim = sim;
        }


        public UUID getSim() {
            return sim;
        }

        public CompoundNBT write() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean("homeless", homeless);
            nbt.putBoolean("hired", hired);
            nbt.putString("sim", sim.toString());

            return nbt;
        }

        public void read(CompoundNBT nbt) {
            homeless = nbt.getBoolean("homeless");
            hired = nbt.getBoolean("hired");
            this.sim = UUID.fromString(nbt.getString("sim"));
        }


    }


    public static class House {
        private final ArrayList<UUID> simOccupants = new ArrayList<>();
        private BlockPos position;
        private String name;
        private float rent;

        public House() {
        }

        private House(BlockPos position, String name, float rent) {
            this.position = position;
            this.name = name;
            this.rent = rent;
        }

        public CompoundNBT write() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("x", position.getX());
            nbt.putInt("y", position.getY());
            nbt.putInt("z", position.getZ());
            nbt.putString("name", name);
            nbt.putFloat("rent", rent);
            ListNBT simIds = new ListNBT();
            for (UUID uuid : simOccupants) {
                CompoundNBT id = new CompoundNBT();
                id.putUUID("uuid", uuid);
                simIds.add(id);

            }
            nbt.put("simids", simIds);
            return nbt;
        }

        public void read(CompoundNBT nbt) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            position = new BlockPos(x, y, z);
            name = nbt.getString("name");
            rent = nbt.getFloat("rent");
            ListNBT ids = nbt.getList("simids", Constants.NBT.TAG_COMPOUND);

            for (INBT inbt : ids) {
                CompoundNBT simId = (CompoundNBT) inbt;
                simOccupants.add(simId.getUUID("uuid"));


            }

        }

        public float getRent() {
            return rent;
        }

        public String getName() {
            return name;
        }

        public BlockPos getPosition() {
            return position;
        }
    }


}
