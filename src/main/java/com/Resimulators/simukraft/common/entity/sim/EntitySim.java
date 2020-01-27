package com.Resimulators.simukraft.common.entity.sim;

import com.Resimulators.simukraft.Configs;
import com.Resimulators.simukraft.common.entity.goals.PickupItemGoal;
import com.Resimulators.simukraft.common.entity.goals.TalkingToPlayerGoal;
import com.Resimulators.simukraft.common.jobs.JobBuilder;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.handlers.FoodStats;
import com.Resimulators.simukraft.init.ModEntities;
import com.Resimulators.simukraft.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class EntitySim extends AgeableEntity implements INPC {
    private static final EntitySize SIZE = EntitySize.flexible(0.6f, 1.8f);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, SIZE).put(Pose.SLEEPING, SLEEPING_SIZE).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
    private static final DataParameter<Integer> VARIATION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FEMALE = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPECIAL = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFTHANDED = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> MODEL_FLAG = EntityDataManager.createKey(EntitySim.class, DataSerializers.BYTE);
    private static final DataParameter<String> STATUS = EntityDataManager.createKey(EntitySim.class, DataSerializers.STRING);
    private static final DataParameter<Integer> NAME_COLOR = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> FOOD_LEVEL = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    public static final DataParameter<Float> FOOD_SATURATION_LEVEL = EntityDataManager.createKey(EntitySim.class, DataSerializers.FLOAT);

    private final SimInventory inventory;
    private PlayerEntity interactingPlayer;

    protected FoodStats foodStats;
    private IJob job;
    private Random rand = new Random();

    public EntitySim(EntityType<? extends AgeableEntity> type, World worldIn) {
        super(ModEntities.ENTITY_SIM, worldIn);
        this.inventory = new SimInventory(this, "Sim Inventory", false, 27);
        this.foodStats = new FoodStats(this);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(VARIATION, 0);
        this.dataManager.register(PROFESSION, 0);
        this.dataManager.register(FEMALE, false);
        this.dataManager.register(SPECIAL, false);
        this.dataManager.register(LEFTHANDED, false);
        this.dataManager.register(MODEL_FLAG, (byte)0);
        this.dataManager.register(STATUS, "");
        this.dataManager.register(NAME_COLOR, 0);
        this.dataManager.register(FOOD_LEVEL, 20);
        this.dataManager.register(FOOD_SATURATION_LEVEL, 5f);
    }

    @Override
    public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficultyInstance, SpawnReason spawnReason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT nbt) {
        ILivingEntityData livingData = super.onInitialSpawn(world, difficultyInstance, spawnReason, livingEntityData, nbt);
        this.setSpecial(Utils.randomizeBooleanWithChance(Configs.SIMS.specialSpawnChance.get()));

        //TODO: Add professions
        //this.setProfession(rand.nextInt(/*Amount of professions*/));

        this.setLefthanded(Utils.randomizeBooleanWithChance(10));

        if (this.getSpecial()) {
            String name = Configs.SIMS.specialSimNames.get().get(rand.nextInt(Configs.SIMS.specialSimNames.get().size()));
            this.setCustomName(new StringTextComponent(name));
            this.setFemale(Configs.SIMS.specialSimGenders.get().contains(name));
        } else {
            this.setFemale(Utils.randomizeBoolean());
            if (this.getFemale()) {
                if (!Configs.NAMES.femaleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(Configs.NAMES.femaleNames.get().get(rand.nextInt(Configs.NAMES.femaleNames.get().size()))));
                this.setVariation(rand.nextInt(13));
            } else {
                if (!Configs.NAMES.maleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(Configs.NAMES.maleNames.get().get(rand.nextInt(Configs.NAMES.maleNames.get().size()))));
                this.setVariation(rand.nextInt(10));
            }
        }

        this.writeAdditional(this.getPersistentData());
        return livingData;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(7, new TalkingToPlayerGoal(this));
        this.goalSelector.addGoal(8, new PickupItemGoal(this));

        //Unimportant "make more alive"-goals
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 2.0f, 1.0f));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomWalkingGoal(this, 0.6d));
        this.goalSelector.addGoal(12, new LookAtGoal(this, PlayerEntity.class, 8f));
        this.goalSelector.addGoal(13, new LookRandomlyGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1);
    }

    @Nullable
    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        EntitySim entitySim = new EntitySim(ModEntities.ENTITY_SIM, world);
        entitySim.onInitialSpawn(this.world, this.world.getDifficultyForLocation(new BlockPos(entitySim)), SpawnReason.BREEDING, new AgeableData(), null);
        return entitySim;
    }

    //Logic
    @Override
    public boolean canDespawn(double p_213397_1_) {
        return false;
    }

    public boolean canPickupStack(@Nonnull ItemStack stack) {
        return Utils.canInsertStack(inventory.getHandler(), stack);
    }

    //NBT Data
    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Variation", this.getVariation());
        compound.putInt("Profession", this.getProfession());
        compound.putBoolean("Female", this.getFemale());
        compound.putBoolean("Special", this.getSpecial());
        compound.putBoolean("Lefthanded", this.getLefthanded());
        compound.put("Inventory", this.inventory.write(new ListNBT()));
        compound.putInt("SelectedItemSlot", this.inventory.currentItem);
        compound.putInt("NameColor", this.getNameColor());
        compound.putString("Status", this.getStatus());
        this.foodStats.write(compound);
        if (job != null){
           compound.put("job", this.job.writeToNbt(new ListNBT()));
        }
    }


    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("Variation"))
            this.setVariation(compound.getInt("Variation"));
        if (compound.contains("Profession"))
            this.setProfession(compound.getInt("Profession"));
        if (compound.contains("Female"))
            this.setFemale(compound.getBoolean("Female"));
        if (compound.contains("Special"))
            this.setSpecial(compound.getBoolean("Special"));
        if (compound.contains("Lefthanded"))
            this.setLefthanded(compound.getBoolean("Lefthanded"));
        if (compound.contains("Inventory"))
            this.inventory.read(compound.getList("Inventory", 10));
        if (compound.contains("SelectedItemSlot"))
            this.inventory.currentItem = compound.getInt("SelectedItemSlot");
        if (compound.contains("NameColor"))
            this.setNameColor(compound.getInt("NameColor"));
        if (compound.contains("Status"))
            this.setStatus(compound.getString("Status"));
        this.foodStats.read(compound);
        String jobType = compound.getList("job", Constants.NBT.TAG_LIST).getCompound(0).getString("jobname");
        switch (jobType){
            case "Builder":
                job = new JobBuilder(this);

        }

        if (compound.contains("job"))
            this.job.readFromNbt(compound.getList("job", Constants.NBT.TAG_LIST));
    }

    //Interaction
    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        if (player.isCrouching()) {
            this.setInteractingPlayer(player);
            player.openContainer(inventory);
        }

        if (player.getHeldItem(hand).getItem() instanceof DyeItem) {
            this.setNameColor(((DyeItem) player.getHeldItem(hand).getItem()).getDyeColor().getId());
        }
        return super.processInteract(player, hand);
    }

    //Updates
    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote()) {
            foodStats.tick(this);
        }
    }

    @Override
    public void livingTick() {
        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
                this.heal(1.0F);
            }


            if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
                this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
            }
        }
        this.inventory.tick();

        super.livingTick();
    }

    public boolean shouldHeal() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        this.dropInventory();
        removeFromFaction();
    }

    //Inventory

    public void selectSlot(int i) {
        if (0 <= i && i < 27)
            inventory.currentItem = i;
    }
    public int getSelectedSlot() {
        return inventory.currentItem;
    }

    @Override
    public void onItemPickup(Entity entity, int quantity) {
        super.onItemPickup(entity, quantity);
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        this.inventory.dropAllItems();
    }

    @Nullable
    public void dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (!droppedItem.isEmpty()) {
            double d0 = this.func_226280_cw_() - (double)0.3F;
            ItemEntity itementity = new ItemEntity(this.world, this.getPosX(), d0, this.getPosZ(), droppedItem);
            itementity.setPickupDelay(40);
            if (traceItem) {
                itementity.setThrowerId(this.getUniqueID());
            }

            if (dropAround) {
                float f = this.rand.nextFloat() * 0.5F;
                float f1 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                itementity.setMotion((double)(-MathHelper.sin(f1) * f), (double)0.2F, (double)(MathHelper.cos(f1) * f));
            } else {
                float f7 = 0.3F;
                float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
                float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
                float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                float f6 = 0.02F * this.rand.nextFloat();
                itementity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos((double)f5) * (double)f6, (double)(-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin((double)f5) * (double)f6);
            }

            this.world.addEntity(itementity);
        }
    }

    public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.inventory.getCurrentItem();
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.inventory.handInventory.get(0);
        } else {
            return slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR ? this.inventory.armorInventory.get(slotIn.getIndex()) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.inventory.mainInventory.set(this.inventory.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.playEquipSound(stack);
            this.inventory.handInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            this.playEquipSound(stack);
            this.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

    public boolean addItemStackToInventory(ItemStack p_191521_1_) {
        this.playEquipSound(p_191521_1_);
        return this.inventory.addItemStackToInventory(p_191521_1_);
    }

    @Override
    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        if (inventorySlot >= 0 && inventorySlot < this.inventory.mainInventory.size()) {
            this.inventory.setInventorySlotContents(inventorySlot, itemStackIn);
            return true;
        } else {
            EquipmentSlotType equipmentslottype;
            if (inventorySlot == 100 + EquipmentSlotType.HEAD.getIndex()) {
                equipmentslottype = EquipmentSlotType.HEAD;
            } else if (inventorySlot == 100 + EquipmentSlotType.CHEST.getIndex()) {
                equipmentslottype = EquipmentSlotType.CHEST;
            } else if (inventorySlot == 100 + EquipmentSlotType.LEGS.getIndex()) {
                equipmentslottype = EquipmentSlotType.LEGS;
            } else if (inventorySlot == 100 + EquipmentSlotType.FEET.getIndex()) {
                equipmentslottype = EquipmentSlotType.FEET;
            } else {
                equipmentslottype = null;
            }

            if (inventorySlot == 98) {
                this.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemStackIn);
                return true;
            } else if (inventorySlot == 99) {
                this.setItemStackToSlot(EquipmentSlotType.OFFHAND, itemStackIn);
                return true;
            } else {
                if (!itemStackIn.isEmpty()) {
                    if (!(itemStackIn.getItem() instanceof ArmorItem) && !(itemStackIn.getItem() instanceof ElytraItem)) {
                        if (equipmentslottype != EquipmentSlotType.HEAD) {
                            return false;
                        }
                    } else if (MobEntity.getSlotForItemStack(itemStackIn) != equipmentslottype) {
                        return false;
                    }
                }

                this.inventory.setInventorySlotContents(equipmentslottype.getIndex() + this.inventory.mainInventory.size(), itemStackIn);
                return true;
            }
        }
    }

    @Override
    public Iterable<ItemStack> getHeldEquipment() {
        return Lists.newArrayList(this.getHeldItemMainhand(), this.getHeldItemOffhand());
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return this.inventory.armorInventory;
    }

    public SimInventory getInventory() {
        return inventory;
    }

    //Data Manager Interaction

    public void setVariation(int variationID) {
        this.dataManager.set(VARIATION, variationID);
    }
    public int getVariation() {
        try {
            return Math.max(this.dataManager.get(VARIATION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setProfession(int professionID) {
        this.dataManager.set(PROFESSION, professionID);
    }

    public int getProfession() {
        try {
            return Math.max(this.dataManager.get(PROFESSION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setFemale(boolean female) {
        this.dataManager.set(FEMALE, female);
    }

    public boolean getFemale() {
        try {
            return this.dataManager.get(FEMALE);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void setSpecial(boolean special) {
        this.dataManager.set(SPECIAL, special);
    }

    public boolean getSpecial() {
        try {
            return this.dataManager.get(SPECIAL);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void setLefthanded(boolean lefthanded) {
        this.dataManager.set(LEFTHANDED, lefthanded);
    }

    public boolean getLefthanded() {
        try {
            return this.dataManager.get(LEFTHANDED);
        } catch (NullPointerException e) {
            return false;
        }

    }

    public void setStatus(String status) {
        this.dataManager.set(STATUS, status);
    }

    public String getStatus() {
        try {
            return this.dataManager.get(STATUS);
        } catch (NullPointerException e) {
            return "";
        }
    }

    public void setInteractingPlayer(PlayerEntity player) {
        this.interactingPlayer = player;
    }

    public PlayerEntity getInteractingPlayer() {
        return this.interactingPlayer;
    }

    public void addExhaustion(float exhaustion) {
        if (!this.isInvulnerable()) {
            if (!this.world.isRemote) {
                this.foodStats.addExhaustion(exhaustion);
            }
        }
    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    public void removeFromFaction() {
        SavedWorldData sWorld = SavedWorldData.get(world);
        ArrayList<Faction> factions = sWorld.getFactions();
        for (Faction faction : factions) {
            if (faction.getSims().containsKey(this.entityUniqueID)) {
                sWorld.removeSimFromFaction(faction.getId(),this);
            }
        }
    }

    public boolean canEat(boolean ignoreHunger) {
        return this.isInvulnerable() || ignoreHunger || this.foodStats.needFood();
    }

    @Override
    public int getGrowingAge() {
        return super.getGrowingAge();
    }

    @Override
    public boolean isChild() {
        return super.isChild();
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return SIZE_BY_POSE.getOrDefault(poseIn, SIZE);
    }

    public void setNameColor(int colorID) {
        if (0 <= colorID && colorID < 16) {
            this.dataManager.set(NAME_COLOR, colorID);
        }
    }

    public int getNameColor() {
        try {
            return this.dataManager.get(NAME_COLOR);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setJob(IJob job) {
        this.job = job;
    }

    public IJob getJob() {
        return job;
    }

    @Override
    public void setCustomName(ITextComponent name) {
        super.setCustomName(name);
        this.inventory.setCustomName(name.getFormattedText());
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isWearing(PlayerModelPart part) {
        return (this.getDataManager().get(MODEL_FLAG) & part.getPartMask()) == part.getPartMask();
    }
}