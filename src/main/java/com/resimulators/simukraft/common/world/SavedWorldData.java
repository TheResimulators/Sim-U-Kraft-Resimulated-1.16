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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;


public class SavedWorldData extends WorldSavedData {
    private static final String DATA_NAME = Reference.MODID + "_SavedWorldData";
    private static final SavedWorldData clientStorageCopy = new SavedWorldData();
    public World world;
    private static Random rand = new Random();
    private HashMap<Integer, Faction> factions = new HashMap<>();

    public SavedWorldData(String name) {
        super(name);
    }

    public SavedWorldData() {
        this(DATA_NAME);
    }

    public SavedWorldData(World world){
        this(DATA_NAME);
        this.world = world;

    }

    /** gets storage data for current world if exists, else creates a new storage instance*/
    public static SavedWorldData get(World world) {
        if (!(world instanceof ServerWorld)) {
            return clientStorageCopy;
        }

        ServerWorld overworld = (ServerWorld) world;

        DimensionSavedDataManager storage = overworld.getSavedData();
        return storage.getOrCreate(() -> new SavedWorldData(world), DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT list = nbt.getList("factions", Constants.NBT.TAG_COMPOUND);
        for (INBT factionNBT:list){
            CompoundNBT compound = (CompoundNBT)factionNBT;
            compound = compound.getCompound("faction");
            int id = compound.getInt("id");
            Faction faction = new Faction(id,world);
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

    /** creates a new faction with a unique random int id and adds it to factions list*/
    public Faction createNewFaction() {
        int id = rand.nextInt();
        while (factions.containsKey(id)) {
            id = rand.nextInt();
        }
        Faction faction = new Faction(id,world);
        factions.put(id, faction);
        markDirty();
        return faction;
    }
    /**deletes faction from list and removes all players and sims from that faction, leaving now reference*/
    public void deleteFaction(int id){
        factions.get(id).removeAllSims();
        factions.get(id).removeAllPlayers();
        factions.remove(id);
        markDirty();

    }

    /** adds a premade faction to faction list. mostly used to update client*/
    public void addFaction(Faction faction){
        setFaction(faction.getId(),faction);
        markDirty();
    }

    /** set faction in list with given id*/
    public void setFaction(int id, Faction faction){
        markDirty();
        this.factions.put(id,faction);
    }
    /**returns faction */
    public Faction getFaction(int id) {
        return factions.get(id);
    }
    /**returns list of faction ids. WIP*/
    public ArrayList<Integer> getFactionIds() {
        return new ArrayList<>(factions.keySet());
    }


    /**gets list of Faction Instances. WIP*/
    public ArrayList<Faction> getFactions() {
        return new ArrayList<>(factions.values());

    }
    /** gets faction with a player in it. searches each faction for the one containing given UUID */
    public Faction getFactionWithPlayer(UUID id){
        for (Faction faction:factions.values()){
            if (faction.getPlayers().contains(id)){
                return faction;
            }
        }
        return null;
    }
    /**gets faction with sim in it, searches through each faction for given UUID*/
    public Faction getFactionWithSim(UUID id){
        for(Faction faction: factions.values()){
            if(faction.containsSim(id)){
                return faction;
            }
        }
        return null;

    }

    /**adds sim to given faction with id, uses sim instance for usefulness*/
    public void addSimToFaction(int id, SimEntity sim){
        factions.get(id).addSim(sim);
        markDirty();
    }
    /**adds player to certain faction with given id, uses playerEntity object to add player */
    public void addPlayerToFaction(int id, PlayerEntity player){
        factions.get(id).addPlayer(player.getUniqueID());
        markDirty();
    }
    /**WIP*/
    public void removePlayerFromFaction(int id, PlayerEntity player){
        // TODO add removal of things
    }

    /**removes sim from faction, all references removes, used for when sim dies.*/
    public void removeSimFromFaction(int id, SimEntity sim){
        this.getFaction(id).removeSim(sim);
        markDirty();
    }

    /** sets sim to be hired, used to see which sims are unemployed*/
    public void hireSim(int id, SimEntity sim){
        getFaction(id).hireSim(sim);
    }
    /**fires sim to make it available for hire*/
    public void fireSim(int id, SimEntity sim){
        getFaction(id).fireSim(sim);
    }
    /**used to clear all stored data*/
    public void clearAll(){
        factions = new HashMap<>();
    }
}
