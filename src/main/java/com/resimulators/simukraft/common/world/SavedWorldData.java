package com.resimulators.simukraft.common.world;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class SavedWorldData extends WorldSavedData {
    private static final String DATA_NAME = Reference.MODID + "_SavedWorldData";
    private static final SavedWorldData clientStorageCopy = new SavedWorldData();
    private static Random rand = new Random();
    private HashMap<Integer, Faction> factions = new HashMap<>();

    public SavedWorldData(String name) {
        super(name);
    }

    public SavedWorldData() {
        this(DATA_NAME);

    }

    public static SavedWorldData get(World world) {
        if (!(world instanceof ServerWorld)) {
            return clientStorageCopy;
        }

        ServerWorld overworld = (ServerWorld) world;

        DimensionSavedDataManager storage = overworld.getSavedData();
        return storage.getOrCreate(SavedWorldData::new, DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT list = nbt.getList("factions", Constants.NBT.TAG_COMPOUND);
        for (INBT factionNBT:list){
            CompoundNBT compound = (CompoundNBT)factionNBT;
            compound = compound.getCompound("faction");
            int id = compound.getInt("id");
            Faction faction = new Faction(id);
            faction.read(compound);

            this.factions.put(id,faction);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (int i :factions.keySet()){
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("faction",factions.get(i).write(new CompoundNBT()));
            list.add(nbt);


        }
        compound.put("factions",list);
        return compound;
    }

    public int getFactionId(Faction faction){
        for (Integer ints:factions.keySet()) {
            if (factions.get(ints) == faction){
                return ints;
            }
        }
        return -1;
    }
    public Faction createNewFaction() {
        int id = rand.nextInt();
        while (factions.containsKey(id)) {
            id = rand.nextInt();
        }
        Faction faction = new Faction(id);
        factions.put(id, faction);
        markDirty();
        return faction;
    }

    public void deleteFaction(int id){
        factions.get(id).removeAllSims();
        factions.get(id).removeAllPlayers();
        factions.remove(id);
        markDirty();

    }
    public void addFaction(Faction faction){
        setFaction(rand.nextInt(),faction);
        markDirty();
    }

    public void setFaction(int id, Faction faction){
        markDirty();
        this.factions.put(id,faction);
    }

    public Faction getFaction(int id) {
        return factions.get(id);
    }

    public ArrayList<Integer> getFactionIds() {
        ArrayList<Integer> factionIds = new ArrayList<>();
        factionIds.addAll(factions.keySet());
        return factionIds;
    }

    public ArrayList<Faction> getFactions() {
        return new ArrayList<>(factions.values());

    }

    public Faction getFactionWithPlayer(UUID id){
        for (Faction faction:factions.values()){
            if (faction.getPlayers().contains(id)){
                return faction;
            }
        }
        return null;
    }

    public Faction getFactionWithSim(UUID id){
        for(Faction faction: factions.values()){
            if(faction.containsSim(id)){
                return faction;
            }
        }
        return null;

    }
    public void addSimToFaction(int id, SimEntity sim){
        factions.get(id).addSim(sim);
        markDirty();
    }

    public void addPlayerToFaction(int id, PlayerEntity player){
        factions.get(id).addPlayer(player.getUniqueID());
        markDirty();
    }

    public void removePlayerFromFaction(int id, PlayerEntity player){
        // TODO add removal of things
    }

    public void removeSimFromFaction(int id, SimEntity sim){
        this.getFaction(id).removeSim(sim);
        markDirty();
    }

    public void hireSim(int id, SimEntity sim){
        getFaction(id).hireSim(sim);
    }

    public void fireSim(int id, SimEntity sim){
        getFaction(id).fireSim(sim);
    }

    public void clearAll(){
        factions = new HashMap<>();
    }
}
