package com.resimulators.simukraft.common.entity.sim;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.goals.CustomWaterAvoidingRandomWalkingGoal;
import com.resimulators.simukraft.common.entity.goals.GoToWorkGoal;
import com.resimulators.simukraft.common.entity.goals.PickupItemGoal;
import com.resimulators.simukraft.common.entity.goals.TalkingToPlayerGoal;
import com.resimulators.simukraft.common.entity.pathfinding.OpenGateGoal;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.FoodStats;
import com.resimulators.simukraft.init.ModEntities;
import com.resimulators.simukraft.init.ModJobs;
import com.resimulators.simukraft.packets.SimFirePacket;
import com.resimulators.simukraft.utils.TextureUtils;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SimEntity extends AgeableEntity implements INPC, IEntityAdditionalSpawnData {
    public static final DataParameter<Integer> FOOD_LEVEL = EntityDataManager.defineId(SimEntity.class, DataSerializers.INT);
    public static final DataParameter<Float> FOOD_SATURATION_LEVEL = EntityDataManager.defineId(SimEntity.class, DataSerializers.FLOAT);
    public static final DataParameter<Optional<UUID>> HOUSE_ID = EntityDataManager.defineId(SimEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final EntitySize SIZE = EntitySize.scalable(0.6f, 1.8f);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, SIZE).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.CROUCHING, EntitySize.scalable(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
    private static final DataParameter<Integer> VARIATION = EntityDataManager.defineId(SimEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.defineId(SimEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> FEMALE = EntityDataManager.defineId(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPECIAL = EntityDataManager.defineId(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFTHANDED = EntityDataManager.defineId(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> MODEL_FLAG = EntityDataManager.defineId(SimEntity.class, DataSerializers.BYTE);
    private static final DataParameter<String> STATUS = EntityDataManager.defineId(SimEntity.class, DataSerializers.STRING);
    private static final DataParameter<Integer> NAME_COLOR = EntityDataManager.defineId(SimEntity.class, DataSerializers.INT);
    private static final int maleSkinCount = 9;
    private static final int femaleSkinCount = 12;

    private final SimInventory inventory;
    protected FoodStats foodStats;
    private PlayerEntity interactingPlayer;
    private IReworkedJob job;
    private Activity activity;
    private WorkingController controller = new WorkingController(this);
    private int houseHuntDelay = 200;

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D) //Movement Speed
                .add(Attributes.MAX_HEALTH, 20.0D) //Health
                .add(Attributes.ATTACK_DAMAGE, 1.0D); //Base Attack Damage
    }

    public SimEntity(World worldIn) {
        this(ModEntities.ENTITY_SIM, worldIn);
    }

    public SimEntity(EntityType<? extends SimEntity> type, World world) {
        super(type, world);
        this.inventory = new SimInventory(this, "Sim Inventory", false, 27);
        this.foodStats = new FoodStats(this);
        ((GroundPathNavigator) this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().getNodeEvaluator().setCanPassDoors(true);

    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new TalkingToPlayerGoal(this));
        this.goalSelector.addGoal(2, new PickupItemGoal(this));

        //Unimportant "make more alive"-goals
        this.goalSelector.addGoal(8, new OpenGateGoal(this,true));
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 2.0f, 1.0f));
        this.goalSelector.addGoal(11, new CustomWaterAvoidingRandomWalkingGoal(this, 0.6d));
        this.goalSelector.addGoal(12, new LookAtGoal(this, PlayerEntity.class, 8f));
        this.goalSelector.addGoal(13, new LookRandomlyGoal(this));
        //Job Important Goals
        this.goalSelector.addGoal(14, new GoToWorkGoal(this));
    }

    @Override
    protected PathNavigator createNavigation(World world) {
        return new SimNavigator(this, world);
    }

    //Updates
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            this.foodStats.tick(this);
            this.controller.tick();
            if (getHouseID() == null) {
                if (houseHuntDelay <= 0) {
                    findHouseToLive();
                } else {
                    houseHuntDelay--;

                }
            }
            /*if (job1 != null)
                job1.tick();*/
        }
    }

    //Logic
    @Override
    public boolean removeWhenFarAway(double p_213397_1_) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return Lists.newArrayList(this.getMainHandItem(), this.getOffhandItem());
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.inventory.armorInventory;
    }

    public ItemStack getItemBySlot(EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.inventory.getCurrentItem();
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.inventory.handInventory.get(0);
        } else {
            return slotIn.getType() == EquipmentSlotType.Group.ARMOR ? this.inventory.armorInventory.get(slotIn.getIndex()) : ItemStack.EMPTY;
        }
    }    //NBT Data    @Nullable    @Override    @Override

    @Override
    public void setItemSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.inventory.mainInventory.set(this.inventory.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.playEquipSound(stack);
            this.inventory.handInventory.set(0, stack);
        } else if (slotIn.getType() == EquipmentSlotType.Group.ARMOR) {
            this.playEquipSound(stack);
            this.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

    //Interaction
    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (player.isCrouching()) {
            this.setInteractingPlayer(player);
            player.openMenu(inventory);
        }

        if (player.getItemInHand(hand).getItem() instanceof DyeItem) {
            this.setNameColor(((DyeItem) player.getItemInHand(hand).getItem()).getDyeColor().getId());
        }
        return super.mobInteract(player, hand);
    }



    @Override
    public boolean setSlot(int inventorySlot, ItemStack itemStackIn) {
        if (inventorySlot >= 0 && inventorySlot < this.inventory.mainInventory.size()) {
            this.inventory.setItem(inventorySlot, itemStackIn);
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
                this.setItemSlot(EquipmentSlotType.MAINHAND, itemStackIn);
                return true;
            } else if (inventorySlot == 99) {
                this.setItemSlot(EquipmentSlotType.OFFHAND, itemStackIn);
                return true;
            } else {
                if (!itemStackIn.isEmpty()) {
                    if (!(itemStackIn.getItem() instanceof ArmorItem) && !(itemStackIn.getItem() instanceof ElytraItem)) {
                        if (equipmentslottype != EquipmentSlotType.HEAD) {
                            return false;
                        }
                    } else if (MobEntity.getEquipmentSlotForItem(itemStackIn) != equipmentslottype) {
                        return false;
                    }
                }

                this.inventory.setItem(equipmentslottype.getIndex() + this.inventory.mainInventory.size(), itemStackIn);
                return true;
            }
        }
    }

    public UUID getHouseID() {
        try {
            return this.entityData.get(HOUSE_ID).get();
        } catch (Exception e) {
            return null;
        }
    }

    public void setHouseID(UUID id) {
        if (id != null) {
            this.entityData.set(HOUSE_ID, Optional.of(id));
        } else {
            this.entityData.set(HOUSE_ID, Optional.empty());
        }
    }

    public void findHouseToLive() {
        Faction faction = SavedWorldData.get(level).getFactionWithSim(this.getUUID());
        if (faction != null) {
            UUID house = faction.getFreeHouse();
            if (house != null) {
                faction.addSimToHouse(house, getUUID());
                faction.sendFactionChatMessage("Sim " + this.getName().getString() + " Has Moved into " + faction.getHouseByID(house).getName().replace("_", " "), level);

                setHouseID(house);
            }
        }
    }

    public boolean canPickupStack(@Nonnull ItemStack stack) {
        return Utils.canInsertStack(inventory.getHandler(), stack);
    }

    public boolean shouldHeal() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.dropEquipment();
        Faction faction = SavedWorldData.get(level).getFactionWithSim(getUUID());
        faction.removeSim(this.getUUID());
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        this.inventory.dropAllItems();
    }

    @Override
    public void take(Entity entity, int quantity) {
        super.take(entity, quantity);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
        //return super.createSpawnPacket();
    }

    @Override
    public EntitySize getDimensions(Pose poseIn) {
        return SIZE_BY_POSE.getOrDefault(poseIn, SIZE);
    }

    public int getSelectedSlot() {
        return inventory.currentItem;
    }

    public void dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (!droppedItem.isEmpty()) {
            double d0 = this.getEyeY() - (double) 0.3F;
            ItemEntity itementity = new ItemEntity(this.level, this.getX(), d0, this.getZ(), droppedItem);
            itementity.setPickUpDelay(40);
            if (traceItem) {
                itementity.setThrower(this.getUUID());
            }

            if (dropAround) {
                float f = level.getRandom().nextFloat() * 0.5F;
                float f1 = level.getRandom().nextFloat() * ((float) Math.PI * 2F);
                itementity.setDeltaMovement(-MathHelper.sin(f1) * f, 0.2F, MathHelper.cos(f1) * f);
            } else {
                float f7 = 0.3F;
                float f8 = MathHelper.sin(this.xRot * ((float) Math.PI / 180F));
                float f2 = MathHelper.cos(this.xRot * ((float) Math.PI / 180F));
                float f3 = MathHelper.sin(this.yRot * ((float) Math.PI / 180F));
                float f4 = MathHelper.cos(this.yRot * ((float) Math.PI / 180F));
                float f5 = level.getRandom().nextFloat() * ((float) Math.PI * 2F);
                float f6 = 0.02F * level.getRandom().nextFloat();
                itementity.setDeltaMovement((double) (-f3 * f2 * 0.3F) + Math.cos(f5) * (double) f6, -f8 * 0.3F + 0.1F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F, (double) (f4 * f2 * 0.3F) + Math.sin(f5) * (double) f6);
            }

            this.level.addFreshEntity(itementity);
        }
    }

    public boolean addItemStackToInventory(ItemStack p_191521_1_) {
        this.playEquipSound(p_191521_1_);
        return this.inventory.addItemStackToInventory(p_191521_1_);
    }

    public SimInventory getInventory() {
        return inventory;
    }

    //resets the sim's inventory
    public void resetInventory(){

        for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            //this might cause a crash.......too bad!
            this.inventory.setItemStack(null);
        }
    }

    public PlayerEntity getInteractingPlayer() {
        return this.interactingPlayer;
    }

    public void setInteractingPlayer(PlayerEntity player) {
        this.interactingPlayer = player;
    }

    public void addExhaustion(float exhaustion) {
        if (!this.isInvulnerable()) {
            if (!this.level.isClientSide) {
                this.foodStats.addExhaustion(exhaustion);
            }
        }
    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    public boolean hasJob() {
        return job != null;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isWearing(PlayerModelPart part) {
        return (this.getEntityData().get(MODEL_FLAG) & part.getMask()) == part.getMask();
    }

        public void fireSim(SimEntity sim, int factionID,boolean dying){
        if (sim.getJob() != null) {
            if (sim.getJob().getWorkSpace() != null){
                SavedWorldData.get(level).fireSim(factionID, sim);
                if (!level.isClientSide) SavedWorldData.get(level).getFaction(factionID).sendPacketToFaction(new SimFirePacket(factionID, sim.getId(), sim.getJob().getWorkSpace(),dying));
                BlockPos jobPos = sim.getJob().getWorkSpace();
                ITile tile = (ITile) sim.level.getBlockEntity(jobPos);
                if (tile != null) {
                    tile.fireSim();
                }
            }
            if (!dying){
                sim.setJob(null);
                sim.setProfession(0);
            }
        }


    }

    public IReworkedJob getJob() {
        return job;
    }

    public void setJob(IReworkedJob job) {
        this.job = job;
    }

    public WorkingController getController() {
        return controller;
    }

    public List<UUID> getOtherHouseOccupants() {
        ArrayList<UUID> list = SavedWorldData.get(level).getFactionWithSim(this.getUUID()).getOccupants(getHouseID());
        list.remove(this.getUUID());
        return list;

    }

    public void moveHouse(UUID currentHouseID, UUID newHouseID) {
        Faction faction = SavedWorldData.get(level).getFactionWithSim(this.getUUID());
        removeFromHouse(faction);
        faction.addSimToHouse(newHouseID, getUUID());
        setHouseID(newHouseID);

    }

    public void removeFromHouse(Faction faction) {
        faction.removeSimFromHouse(getHouseID(), this.getUUID());
        removeHouse();
    }

    public void removeHouse() {
        setHouseID(null);
    }

    public boolean isHomeless() {
        return getHouseID() == null;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(job != null);
        if (job != null) {
            buffer.writeInt(this.getProfession());
            CompoundNBT nbt = new CompoundNBT();
            ListNBT list = job.writeToNbt(new ListNBT());
            buffer.writeBoolean(list != null);
            if (list != null) nbt.put("nbt", list);
            buffer.writeNbt(nbt);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        if (additionalData.readBoolean()) {
            int id = additionalData.readInt();
            if (id > 0 && additionalData.readBoolean()) {
                job = ModJobs.JOB_LOOKUP.get(id).apply(this);
                ListNBT nbt = additionalData.readNbt().getList("nbt", Constants.NBT.TAG_COMPOUND);
                job.readFromNbt(nbt);
            }
        }
    }

    public int getProfession() {
        try {
            return Math.max(this.entityData.get(PROFESSION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setProfession(int professionID) {
        this.entityData.set(PROFESSION, professionID);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIATION, 0);
        this.entityData.define(PROFESSION, 0);
        this.entityData.define(FEMALE, false);
        this.entityData.define(SPECIAL, false);
        this.entityData.define(LEFTHANDED, false);
        this.entityData.define(MODEL_FLAG, (byte) 0);
        this.entityData.define(STATUS, "");
        this.entityData.define(NAME_COLOR, 0);
        this.entityData.define(FOOD_LEVEL, 20);
        this.entityData.define(FOOD_SATURATION_LEVEL, 5f);
        this.entityData.define(HOUSE_ID, Optional.empty());
    }


    public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
        this.setSpecial(Utils.randomizeBooleanWithChance(SimuKraft.config.getSims().specialSpawnChance.get()));

        //TODO: Add professions
        //this.setProfession(rand.nextInt(/*Amount of professions*/));

        this.setLefthanded(Utils.randomizeBooleanWithChance(10));

        if (this.getSpecial()) {
            String name = SimuKraft.config.getSims().specialSimNames.get().get(level.getRandom().nextInt(SimuKraft.config.getSims().specialSimNames.get().size()));
            this.setCustomName(new StringTextComponent(name));
            this.setFemale(SimuKraft.config.getSims().specialSimGenders.get().contains(name));
        } else {
            this.setFemale(Utils.randomizeBoolean());
            if (this.getFemale()) {
                if (!SimuKraft.config.getNames().femaleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(SimuKraft.config.getNames().femaleNames.get().get(level.getRandom().nextInt(SimuKraft.config.getNames().femaleNames.get().size()))));
                    this.setVariation(level.getRandom().nextInt(femaleSkinCount));

            } else {
                if (!SimuKraft.config.getNames().maleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(SimuKraft.config.getNames().maleNames.get().get(level.getRandom().nextInt(SimuKraft.config.getNames().maleNames.get().size()))));
                    this.setVariation(level.getRandom().nextInt(maleSkinCount));
            }
        }
        this.getNavigation().getNodeEvaluator().setCanOpenDoors(true);
        this.getNavigation().getNodeEvaluator().setCanPassDoors(true);

        this.addAdditionalSaveData(this.getPersistentData());
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }


    @Override
    public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity ageable) {
        SimEntity simEntity = new SimEntity(world);
        simEntity.finalizeSpawn(world, this.level.getCurrentDifficultyAt(simEntity.blockPosition()), SpawnReason.BREEDING, new AgeableData(true), null);
        return simEntity;
    }


    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variation", this.getVariation());
        compound.putInt("Profession", this.getProfession());
        compound.putBoolean("Female", this.getFemale());
        compound.putBoolean("Special", this.getSpecial());
        compound.putBoolean("Lefthanded", this.getLefthanded());
        compound.put("Inventory", this.inventory.write(new ListNBT()));
        compound.putInt("SelectedItemSlot", this.inventory.currentItem);
        compound.putInt("NameColor", this.getNameColor());
        compound.putString("Status", this.getStatus());
        compound.putInt("activity", this.getActivity().id);
        this.foodStats.write(compound);
        if (job != null) {
            compound.put("job", this.job.writeToNbt(new ListNBT()));
        }


        if (controller != null) {
            compound.put("working controller", controller.serializeNBT());
        }
        compound.putUUID("uuid", getUUID());
    }


    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
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
        ListNBT nbt = compound.getList("job", Constants.NBT.TAG_COMPOUND);

        int jobType = nbt.getCompound(0).getInt("id");
        if (jobType != 0) {
            job = ModJobs.JOB_LOOKUP.get(jobType).apply(this);
        }
        if (compound.contains("job") && job != null) {
            this.job.readFromNbt((ListNBT) compound.get("job"));
        }
        controller = new WorkingController(this);
        if (compound.contains("working controller")) {
            controller.deserializeNBT(compound.getCompound("working controller"));
        }
        if (compound.hasUUID("uuid")) {
            this.uuid = compound.getUUID("uuid");
        }

        if (compound.contains("activity"))
            setActivity(Activity.getActivityById(compound.getInt("activity")));
        //NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    public void aiStep() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
                this.heal(1.0F);
            }


            if (this.foodStats.needFood() && this.tickCount % 10 == 0) {
                this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
            }
        } else {
            if (this.foodStats.shouldEat() || this.getHealth() < 12) {
                if (this.canEat(false)) {
                    this.startUsingItem(this.getUsedItemHand());
                    this.selectSlot(this.inventory.getSlotFor(this.inventory.getFood()));
                    ItemStack stack = this.getMainHandItem();
                    Food food = stack.getItem().getFoodProperties();
                    if (food != null) {
                        this.foodStats.consume(stack.getItem(), stack);
                    }
                }
            }
        }
        this.inventory.tick();

        super.aiStep();
        updateSwingTime();
    }

    protected void updateSwingTime() {
        int i = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }

        this.attackAnim = (float)this.swingTime / (float)i;
    }


    private int getCurrentSwingDuration(){
        return 8;
    }
    public void selectSlot(int i) {
        if (0 <= i && i < 27)
            inventory.currentItem = i;
    }


    public void setVariation(int variationID) {
        this.entityData.set(VARIATION, variationID);
    }


    public int getVariation() {
        try {
            return Math.max(this.entityData.get(VARIATION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }


    public void setFemale(boolean female) {
        this.entityData.set(FEMALE, female);
    }


    public boolean getFemale() {
        try {
            return this.entityData.get(FEMALE);
        } catch (NullPointerException e) {
            return false;
        }
    }


    public void setSpecial(boolean special) {
        this.entityData.set(SPECIAL, special);
    }


    public boolean getSpecial() {
        try {
            return this.entityData.get(SPECIAL);
        } catch (NullPointerException e) {
            return false;
        }
    }


    public void setLefthanded(boolean lefthanded) {
        this.entityData.set(LEFTHANDED, lefthanded);
    }


    public boolean getLefthanded() {
        try {
            return this.entityData.get(LEFTHANDED);
        } catch (NullPointerException e) {
            return false;
        }

    }


    public void setStatus(String status) {
        this.entityData.set(STATUS, status);
    }


    public String getStatus() {
        try {
            return this.entityData.get(STATUS);
        } catch (NullPointerException e) {
            return "";
        }
    }


    public boolean canEat(boolean ignoreHunger) {
        return this.isInvulnerable() || ignoreHunger || this.foodStats.needFood();
    }

    @Override
    public int getAge() {
        return super.getAge();
    }

    @Override
    public boolean isBaby() {
        return super.isBaby();
    }


    public void setNameColor(int colorID) {
        if (0 <= colorID && colorID < 16) {
            this.entityData.set(NAME_COLOR, colorID);
        }
    }

    public int getNameColor() {
        try {
            return this.entityData.get(NAME_COLOR);
        } catch (NullPointerException e) {
            return 0;
        }
    }


    @Override
    public void setCustomName(ITextComponent name) {
        super.setCustomName(name);
        this.inventory.setCustomName(name.getString());
    }


    public Activity getActivity() {
        if (activity == null) {
            setActivity(Activity.IDLING);
        }
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}