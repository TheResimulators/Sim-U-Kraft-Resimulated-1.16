package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Faction {
    private ArrayList<UUID> players = new ArrayList<>();
    private HashMap<UUID,SimInfo> sims = new HashMap<>();
    private double credits = 0d;
    private int id;
    private static Random rand = new Random();
    //TODO: add Housing to this

    public Faction(int id){
        this.id = id;
    }
    public CompoundNBT write(CompoundNBT nbt){
        ListNBT list = new ListNBT();
        for(UUID uuid: players){
            CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId("player",uuid);
            list.add(compound);


        }
        nbt.put("players",list);

        list = new ListNBT();
        for (UUID id:sims.keySet()){
            CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId("sim",id);
            compound.put("siminfo",sims.get(id).write());
            list.add(compound);
        }
        nbt.put("sims",list);
        nbt.putDouble("credits",credits);
        nbt.putInt("id",id);
        return nbt;
    }

    public void read(CompoundNBT nbt){
        ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
        for (INBT player : players) {
            CompoundNBT compound = (CompoundNBT) player;
            this.players.add(compound.getUniqueId("player"));
        }
        ListNBT sims = nbt.getList("sims",Constants.NBT.TAG_COMPOUND);

        for (INBT sim : players){
            CompoundNBT compound = (CompoundNBT) sim;
            UUID id =compound.getUniqueId("sim");
            SimInfo info = new SimInfo(id);
            info.read(compound.getCompound("siminfo"));
            this.sims.put(id,info);

        }
        this.credits = nbt.getDouble("credits");

    }

    public void addsim(EntitySim sim){

        sims.put(sim.getUniqueID(), new SimInfo(sim.getUniqueID()));
    }

    public void setCredits(double credits){
        this.credits = credits;
    }

    public void addCredits(double credits){
        setCredits(this.credits +credits);
    }

    public double getCredits(){
        return this.credits;
    }

    public ArrayList<UUID> getUnemployedSims(){
        ArrayList<UUID> sims = new ArrayList<>();

        for (SimInfo info:this.sims.values()) {
            if (!info.hired){
                sims.add(info.sim);
            }


        }
        return sims;
    }

    public void hireSim(UUID id){
        this.sims.get(id).hired = true;
    }

    public void hireSim(EntitySim sim){
        this.hireSim(sim.getUniqueID());
    }

    public void fireSim(UUID id){
        this.sims.get(id).hired = false;
    }

    public void fireSim(EntitySim sim) {
        this.fireSim(sim.getUniqueID());
    }

    public int getAmountOfSims() {
        return sims.size();
    }



    public ArrayList<Integer> getSimIds(ServerWorld world){
        ArrayList<Integer> simids = new ArrayList<>();
        for (UUID id:sims.keySet()){
            simids.add(world.getEntityByUuid(id).getEntityId());
        }
        return simids;
    }
    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID player){
        this.players.add(player);
    }

    public int getId(){
        return id;
    }

    static class SimInfo {
        private UUID sim;
        private boolean hired;
        private boolean homeless;

        SimInfo(UUID sim) {
            this.sim = sim;
        }


        public UUID getSim(){
            return sim;
        }

        public CompoundNBT write(){
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean("homeless",homeless);
            nbt.putBoolean("hired",hired);
            nbt.putString("sim",sim.toString());

            return nbt;
        }

        public void read(CompoundNBT nbt){
            homeless = nbt.getBoolean("homeless");
            hired = nbt.getBoolean("hired");
            this.sim = UUID.fromString(nbt.getString("sim"));
        }



    }
}
