package com.Resimulators.simukraft.common.capabilities;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.world.Faction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCapability implements INBTSerializable<CompoundNBT> {
    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Reference.MODID, "player_capability");
    private Faction faction;
    private int factionId;
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (faction != null){
            nbt.putInt("id",factionId);
            nbt.put("faction", faction.write(new CompoundNBT()));}
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        faction = new Faction(nbt.getInt("id"));
        faction.read(nbt.getCompound("faction"));
        factionId = nbt.getInt("id");
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        if (faction != null){
        this.factionId = faction.getId();
        this.faction = faction;
    }}

    public int getFactionId() {
        return factionId;
    }

    public static class Provider implements ICapabilitySerializable<INBT> {
        @CapabilityInject(PlayerCapability.class)
        public static Capability<PlayerCapability> TEST;
        final PlayerCapability capability = new PlayerCapability();
        final LazyOptional<PlayerCapability> PlayerSupplier = LazyOptional.of(PlayerCapability::new);

        @Override
        public INBT serializeNBT() {
            return capability.serializeNBT();
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            capability.deserializeNBT((CompoundNBT) nbt);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return getCapability(cap);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
            {
                if (TEST == cap) return PlayerSupplier.cast();
                return LazyOptional.empty();
            }
        }

    }



    public static class Storage implements Capability.IStorage<PlayerCapability> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<PlayerCapability> capability, PlayerCapability instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<PlayerCapability> capability, PlayerCapability instance, Direction side, INBT nbt) {
        }
    }
}