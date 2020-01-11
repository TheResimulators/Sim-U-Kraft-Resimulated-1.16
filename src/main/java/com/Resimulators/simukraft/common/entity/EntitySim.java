package com.Resimulators.simukraft.common.entity;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.init.ModEntities;
import com.sun.istack.internal.NotNull;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class EntitySim extends AgeableEntity implements INPC {
    public static final DataParameter<Float> HUNGER = EntityDataManager.createKey(EntitySim.class, DataSerializers.FLOAT);
    public static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> FEMALE = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> VARIATION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);



    private ResourceLocation skin_texture;
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
    protected void registerData(){
        this.dataManager.register(HUNGER,20.0f);
        this.dataManager.register(PROFESSION,0);
        this.dataManager.register(FEMALE,false);
        this.dataManager.register(VARIATION,0);

    }


    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0,new SwimGoal(this));
        this.goalSelector.addGoal(1,new WaterAvoidingRandomWalkingGoal(this,0.7d));
        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class,0.5f));
        this.goalSelector.addGoal(3,new LookRandomlyGoal(this));
        //4 and 5 reserved for job ai's
        this.goalSelector.addGoal(4,new OpenDoorGoal(this,true));

    }
    @ParametersAreNonnullByDefault
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
        CompoundNBT nbt = new CompoundNBT();






        return nbt;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
    }




    public float getHunger(){
        return this.dataManager.get(HUNGER);
    }

    public void setHunger(){
        this.dataManager.set(HUNGER,20.0f);
    }

    public int getVariation(){
        return Math.max(this.dataManager.get(VARIATION),0);
    }
    public void setVariation(int variationid){
       this.dataManager.set(VARIATION,variationid);
    }

    public void setProfession(int professionid){
        this.dataManager.set(PROFESSION,0);
    }

    public int getProfession(){
        return Math.max(this.dataManager.get(PROFESSION),0);
    }

    public boolean getFemale (){
        return this.dataManager.get(FEMALE);
    }
    public void setFemale(boolean female){
        this.dataManager.set(FEMALE,female);
    }

    public String getLabeledProfession() {
        switch (getProfession()) {
            default:
                return "Nitwit";
            case 0:
                return "NitWit";
            case 1:
                return "Builder";
            case 2:
                return "Farmer";
            case 3:
                return "Fisher";
            case 4:
                return "Butcher";
            case 5:
                return "Cattle Farmer";
            case 6:
                return "Sheep Farmer";
            case 7:
                return "Miner";
        }
    }
}