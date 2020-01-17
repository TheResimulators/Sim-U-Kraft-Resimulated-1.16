package com.Resimulators.simukraft.common.world;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.naming.ldap.LdapReferralException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Faction {
    private HashMap<UUID,SimInfo> players = new HashMap<>();
    private HashMap<UUID,SimInfo> sims = new HashMap<>();
    private double credits = 0d;
    private static Random rand = new Random();
    //TODO: add Housing to this

    public CompoundNBT write(CompoundNBT nbt){
        nbt.putDouble("test",5d);
        return nbt;
    }

    public void read(CompoundNBT nbt){

    }

    public void addsim(EntitySim sim){

        sims.put(sim.getUniqueID(), new SimInfo(sim));
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

    public ArrayList<EntitySim> getUnemployedSims(){
        ArrayList<EntitySim> sims = new ArrayList<EntitySim>();

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
    public void fireSim(EntitySim sim){
        this.fireSim(sim.getUniqueID());
    }
    static class SimInfo {
        private EntitySim sim;
        private boolean hired;
        private boolean homeless;
        SimInfo(EntitySim sim){
            this.sim = sim;
        }


        public EntitySim getSim(){
            return sim;
        }





    }
}
