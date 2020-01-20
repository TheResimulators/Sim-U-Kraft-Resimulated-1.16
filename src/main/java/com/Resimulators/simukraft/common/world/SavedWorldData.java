package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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

        ServerWorld overworld = world.getServer().getWorld(DimensionType.OVERWORLD);

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
        ArrayList<Faction> allFactions = new ArrayList<Faction>(factions.values());
        return allFactions;
    }

    public Faction getFactionWithPlayer(UUID id){
        for (Faction faction:factions.values()){
            if (faction.getPlayers().contains(id)){
                return faction;
            }
        }
        return null;
    }

    public void addSimToFaction(int id, EntitySim sim){
        factions.get(id).addsim(sim);
        markDirty();
    }

    public void addPlayerToFaction(int id, PlayerEntity player){
        factions.get(id).addPlayer(player.getUniqueID());
        markDirty();
    }

    public void removePlayerFromFaction(int id, PlayerEntity player){
        // TODO add removal of things
    }

    public void removeSimFromFaction(int id, EntitySim sim){
        //TODO add removal of sims
    }

    public void hireSim(int id,EntitySim sim){
        getFaction(id).hireSim(sim);
    }

    public void fireSim(int id,EntitySim sim){
        getFaction(id).fireSim(sim);
    }

}
