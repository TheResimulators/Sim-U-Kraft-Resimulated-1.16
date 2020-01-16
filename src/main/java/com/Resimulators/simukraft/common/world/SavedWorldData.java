package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.lang.reflect.Array;
import java.util.*;

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

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        return null;
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
        Faction faction = new Faction();
        factions.put(id, faction);
        return faction;
    }

    public void deleteFaction(int id){
        factions.remove(id);

    }


    public Faction getFaction(int id){
            return factions.get(id);
    }

    public ArrayList<Integer> getFactionIds(){
        ArrayList<Integer> factionIds = new ArrayList<>();
        factionIds.addAll(factions.keySet());
        return factionIds;
    }

}
