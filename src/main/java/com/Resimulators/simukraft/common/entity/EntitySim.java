package com.Resimulators.simukraft.common.entity;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.init.ModEntities;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySim extends AgeableEntity implements INPC {
    ResourceLocation skin_texture;
    private static final int SIM_TEXTURE_COUNT = 6;
    public EntitySim(EntityType<? extends AgeableEntity> type, World worldIn) {
        super(ModEntities.ENTITY_SIM, worldIn);
        int skin_texture_number = (int)Math.floor(Math.random() * SIM_TEXTURE_COUNT + 1);
        this.skin_texture = new ResourceLocation(Reference.MODID + ":textures/entity/entity_sim" + skin_texture_number + ".png");
    }

    public ResourceLocation getSkin() {
        return this.skin_texture;
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
        EntitySim entitySim = new EntitySim(ModEntities.ENTITY_SIM, world);
        entitySim.onInitialSpawn(this.world, this.world.getDifficultyForLocation(new BlockPos(entitySim)), SpawnReason.BREEDING, new AgeableData(), null);
        return entitySim;
    }


    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    @Override
    public CompoundNBT serializeNBT() {
        return null;
    }
}