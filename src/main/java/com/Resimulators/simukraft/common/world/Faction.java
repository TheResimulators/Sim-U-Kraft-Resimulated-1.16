package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.Network;
import com.Resimulators.simukraft.SimuKraft;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.packets.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Faction {
    private ArrayList<UUID> players = new ArrayList<>();
    private HashMap<UUID, SimInfo> sims = new HashMap<>();
    private double credits = 0d;
    private int id;
    private static Random rand = new Random();
    //TODO: add Housing to this

    public Faction(int id) {
        this.id = id;
    }

    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (UUID uuid : players) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId("player", uuid);
            list.add(compound);
        }
        nbt.put("players", list);

        list = new ListNBT();
        for (UUID id : sims.keySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId("sim", id);
            compound.put("siminfo", sims.get(id).write());
            list.add(compound);
        }
        nbt.put("sims", list);
        nbt.putDouble("credits", credits);
        nbt.putInt("id", id);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
        for (INBT player : players) {
            CompoundNBT compound = (CompoundNBT) player;
            this.players.add(compound.getUniqueId("player"));
        }
        ListNBT sims = nbt.getList("sims", Constants.NBT.TAG_COMPOUND);

        for (INBT sim : sims) {
            CompoundNBT compound = (CompoundNBT) sim;
            UUID id = compound.getUniqueId("sim");
            SimInfo info = new SimInfo(id);
            info.read(compound.getCompound("siminfo"));
            this.sims.put(id, info);
        }
        this.credits = nbt.getDouble("credits");
    }

    public void addSim(EntitySim sim) {
        addSim(sim.getUniqueID());
    }

    public void addSim(UUID id) {
        sims.put(id, new SimInfo(id));
    }

    public void removeSim(EntitySim sim) {
        sims.remove(sim.getUniqueID());
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public void addCredits(double credits) {
        setCredits(this.credits + credits);
    }

    public double getCredits() {
        return this.credits;
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

    public void hireSim(UUID id) {
        this.sims.get(id).hired = true;
    }

    public void hireSim(EntitySim sim) {
        this.hireSim(sim.getUniqueID());
    }

    public void fireSim(UUID id) {
        this.sims.get(id).hired = false;
    }

    public void fireSim(EntitySim sim) {
        this.fireSim(sim.getUniqueID());
    }

    public int getAmountOfSims() {
        return sims.size();
    }

    public ArrayList<Integer> getSimIds(ServerWorld world) {
        ArrayList<Integer> simids = new ArrayList<>();
        for (UUID id : sims.keySet()) {
            Entity entity = world.getEntityByUuid(id);
            if (entity != null) {
                simids.add(entity.getEntityId());
            } else {
                SimuKraft.LOGGER().error("Error: Entity doesn't exist in faction. Please contact the author.");
            }
        }
        return simids;
    }

    public ArrayList<Integer> getSimUnemployedIds(ServerWorld world) {
        ArrayList<Integer> simids = new ArrayList<>();
        for (UUID id : sims.keySet()) {
            Entity entity = world.getEntityByUuid(id);
            if (entity != null) {
                if (!sims.get(id).hired) {
                    simids.add(entity.getEntityId());
                }
            } else {
                SimuKraft.LOGGER().error("Error: Unemployed entity doesn't exist in faction. Please contact the author.");
            }
        }
        return simids;
    }

    public HashMap<UUID, SimInfo> getSims() {
        return sims;
    }

    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID player) {
        this.players.add(player);
    }

    public int getId() {
        return id;
    }

    public void removeAllSims() {
        sims.keySet().iterator().forEachRemaining(ID -> sims.remove(ID));
    }

    public void removeAllPlayers() {
        players.iterator().forEachRemaining(PLAYER -> players.remove(PLAYER));
    }

    public void sendPacketToFaction(IMessage message) {
        for (UUID id : players) {
            ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(id);
            if (player != null) {
                Network.handler.sendToPlayer(message, player);
            }
        }
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

    static class SimInfo {
        private UUID sim;
        private boolean hired;
        private boolean homeless;

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
}
