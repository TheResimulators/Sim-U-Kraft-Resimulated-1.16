package com.Resimulators.simukraft.common.entity;

import com.Resimulators.simukraft.init.ModEntities;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySim extends AgeableEntity implements INPC {
    @SuppressWarnings("unchecked")
    public EntitySim(EntityType<? extends AgeableEntity> type, World worldIn) {
        super((EntityType<? extends AgeableEntity>) ModEntities.ENTITY_SIM, worldIn);
    }


    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0,new SwimGoal(this));
        this.goalSelector.addGoal(1,new RandomWalkingGoal(this,0.7d));
        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class,0.5f));
        this.goalSelector.addGoal(3,new LookRandomlyGoal(this));


    }



    @Nullable
    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        return null;
    }


    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    @Override
    public CompoundNBT serializeNBT() {
        return null;
    }
}