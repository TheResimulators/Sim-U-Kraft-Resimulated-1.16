package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

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
        return faction;
    }

    public void deleteFaction(int id){
        factions.remove(id);
    }

    public void setFaction(int id, Faction faction){
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

}
