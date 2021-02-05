package com.resimulators.simukraft.common.entity.sim;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.goals.GoToWorkGoal;
import com.resimulators.simukraft.common.entity.goals.PickupItemGoal;
import com.resimulators.simukraft.common.entity.goals.TalkingToPlayerGoal;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.core.IJob;
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
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class SimEntity extends AgeableEntity implements INPC {
    private static final EntitySize SIZE = EntitySize.flexible(0.6f, 1.8f);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, SIZE).put(Pose.SLEEPING, SLEEPING_SIZE).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
    private static final DataParameter<Integer> VARIATION = EntityDataManager.createKey(SimEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(SimEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FEMALE = EntityDataManager.createKey(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPECIAL = EntityDataManager.createKey(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFTHANDED = EntityDataManager.createKey(SimEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> MODEL_FLAG = EntityDataManager.createKey(SimEntity.class, DataSerializers.BYTE);
    private static final DataParameter<String> STATUS = EntityDataManager.createKey(SimEntity.class, DataSerializers.STRING);
    private static final DataParameter<Integer> NAME_COLOR = EntityDataManager.createKey(SimEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> FOOD_LEVEL = EntityDataManager.createKey(SimEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Float> FOOD_SATURATION_LEVEL = EntityDataManager.createKey(SimEntity.class, DataSerializers.FLOAT);

    private final SimInventory inventory;
    private PlayerEntity interactingPlayer;

    protected FoodStats foodStats;
    private IJob job;
    private Activity activity;
    private WorkingController controller = new WorkingController(this);
    private Random rand = new Random();
    private UUID houseID;
    private int houseHuntDelay = 200;
    public SimEntity(World worldIn) {
        this(ModEntities.ENTITY_SIM, worldIn);
    }

    public SimEntity(EntityType<? extends SimEntity> type, World world) {
        super(type, world);
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
        this.dataManager.register(MODEL_FLAG, (byte) 0);
        this.dataManager.register(STATUS, "");
        this.dataManager.register(NAME_COLOR, 0);
        this.dataManager.register(FOOD_LEVEL, 20);
        this.dataManager.register(FOOD_SATURATION_LEVEL, 5f);
    }

    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
        this.setSpecial(Utils.randomizeBooleanWithChance(SimuKraft.config.getSims().specialSpawnChance.get()));

        //TODO: Add professions
        //this.setProfession(rand.nextInt(/*Amount of professions*/));

        this.setLefthanded(Utils.randomizeBooleanWithChance(10));

        if (this.getSpecial()) {
            String name = SimuKraft.config.getSims().specialSimNames.get().get(rand.nextInt(SimuKraft.config.getSims().specialSimNames.get().size()));
            this.setCustomName(new StringTextComponent(name));
            this.setFemale(SimuKraft.config.getSims().specialSimGenders.get().contains(name));
        } else {
            this.setFemale(Utils.randomizeBoolean());
            if (this.getFemale()) {
                if (!SimuKraft.config.getNames().femaleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(SimuKraft.config.getNames().femaleNames.get().get(rand.nextInt(SimuKraft.config.getNames().femaleNames.get().size()))));
                try {
                    this.setVariation(rand.nextInt(Objects.requireNonNull(TextureUtils.getAllFilesInFolder("textures/entity/sim/female")).size()));
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!SimuKraft.config.getNames().maleNames.get().isEmpty())
                    this.setCustomName(new StringTextComponent(SimuKraft.config.getNames().maleNames.get().get(rand.nextInt(SimuKraft.config.getNames().maleNames.get().size()))));
                try {
                    this.setVariation(rand.nextInt((Objects.requireNonNull(TextureUtils.getAllFilesInFolder("textures/entity/sim/male")).size())));
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.getNavigator().getNodeProcessor().setCanOpenDoors(true);
        this.getNavigator().getNodeProcessor().setCanEnterDoors(true);

        this.writeAdditional(this.getPersistentData());
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new TalkingToPlayerGoal(this));
        this.goalSelector.addGoal(2, new PickupItemGoal(this));

        //Unimportant "make more alive"-goals
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 2.0f, 1.0f));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomWalkingGoal(this, 0.6d));
        this.goalSelector.addGoal(12, new LookAtGoal(this, PlayerEntity.class, 8f));
        this.goalSelector.addGoal(13, new LookRandomlyGoal(this));
        //Job Important Goals
        this.goalSelector.addGoal(14, new GoToWorkGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute getAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.5D) //Movement Speed
                .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D) //Health
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D); //Base Attack Damage
    }

    @Nullable
    @Override
    public AgeableEntity func_241840_a(ServerWorld world, AgeableEntity ageable) {
        SimEntity simEntity = new SimEntity(world);
        simEntity.onInitialSpawn(world, this.world.getDifficultyForLocation(simEntity.getPosition()), SpawnReason.BREEDING, new AgeableData(true), null);
        return simEntity;
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
        compound.putInt("activity", this.getActivity().id);
        this.foodStats.write(compound);
        if (job != null) {
            compound.put("job", this.job.writeToNbt(new ListNBT()));
        }

        if (controller != null) {
            compound.put("working controller", controller.serializeNBT());
        }
        compound.putUniqueId("uuid",getUniqueID());
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
       ListNBT nbt = compound.getList("job", Constants.NBT.TAG_COMPOUND);

        int jobType = nbt.getCompound(0).getInt("id");
        job = ModJobs.JOB_LOOKUP.get(jobType).apply(this);

        if (compound.contains("job") && job != null){
            this.job.readFromNbt((ListNBT) compound.get("job"));
        }
        controller = new WorkingController(this);
        if (compound.contains("working controller")) {
            controller.deserializeNBT(compound.getCompound("working controller"));
        }
        if (compound.hasUniqueId("uuid")){
            this.entityUniqueID = compound.getUniqueId("uuid");
        }

        if (compound.contains("activity"))
            setActivity(Activity.getActivityById(compound.getInt("activity")));
    }


    //Interaction
    @Override
    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        if (player.isCrouching()) {
            this.setInteractingPlayer(player);
            player.openContainer(inventory);
        }

        if (player.getHeldItem(hand).getItem() instanceof DyeItem) {
            this.setNameColor(((DyeItem) player.getHeldItem(hand).getItem()).getDyeColor().getId());
        }
        return super.func_230254_b_(player, hand);
    }

    //Updates
    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote()) {
            this.foodStats.tick(this);
            this.controller.tick();
            if (houseID == null){
                if (houseHuntDelay <= 0){
                    findHouseToLive();
                }else{
                    houseHuntDelay--;

                }
            }
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
        } else {
            if (this.foodStats.shouldEat()) {
                if (this.canEat(false)) {
                    this.setActiveHand(this.getActiveHand());
                    this.selectSlot(this.inventory.getSlotFor(this.inventory.getFood()));
                    ItemStack stack = this.getHeldItemMainhand();
                    Food food = stack.getItem().getFood();
                    if (food != null) {
                        this.foodStats.consume(stack.getItem(), stack);
                    }
                }
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
            double d0 = this.getPosYEye() - (double) 0.3F;
            ItemEntity itementity = new ItemEntity(this.world, this.getPosX(), d0, this.getPosZ(), droppedItem);
            itementity.setPickupDelay(40);
            if (traceItem) {
                itementity.setThrowerId(this.getUniqueID());
            }

            if (dropAround) {
                float f = this.rand.nextFloat() * 0.5F;
                float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
                itementity.setMotion((double) (-MathHelper.sin(f1) * f), (double) 0.2F, (double) (MathHelper.cos(f1) * f));
            } else {
                float f7 = 0.3F;
                float f8 = MathHelper.sin(this.rotationPitch * ((float) Math.PI / 180F));
                float f2 = MathHelper.cos(this.rotationPitch * ((float) Math.PI / 180F));
                float f3 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F));
                float f4 = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F));
                float f5 = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f6 = 0.02F * this.rand.nextFloat();
                itementity.setMotion((double) (-f3 * f2 * 0.3F) + Math.cos((double) f5) * (double) f6, (double) (-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double) (f4 * f2 * 0.3F) + Math.sin((double) f5) * (double) f6);
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
                sWorld.removeSimFromFaction(faction.getId(), this);
                if (job != null){
                if (job.getWorkSpace() != null) { //only temporary until we get the job system done
                    ((ITile) world.getTileEntity(job.getWorkSpace())).setSimId(null);
                    ((ITile) world.getTileEntity(job.getWorkSpace())).setHired(false);
                    }
                }
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

    public boolean hasJob(){
        return job != null;
    }
    @Override
    public void setCustomName(ITextComponent name) {
        super.setCustomName(name);
        this.inventory.setCustomName(name.getString());
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isWearing(PlayerModelPart part) {
        return (this.getDataManager().get(MODEL_FLAG) & part.getPartMask()) == part.getPartMask();
    }


    public void fireSim(SimEntity sim, int id,boolean dying){
        if (sim.getJob() != null) {
            if (sim.getJob().getWorkSpace() != null){
                SavedWorldData.get(world).fireSim(id, sim);
                if (!world.isRemote) SavedWorldData.get(world).getFaction(id).sendPacketToFaction(new SimFirePacket(id, sim.getEntityId(), sim.getJob().getWorkSpace()));
                BlockPos jobPos = sim.getJob().getWorkSpace();
                ITile tile = (ITile) sim.world.getTileEntity(jobPos);
                if (tile != null) {
                    tile.setHired(false);
                    tile.setSimId(null);
                }
            }
            if (!dying){
                sim.getJob().removeJobAi();
                sim.setJob(null);
                sim.setProfession(0);
            }else{
                SavedWorldData.get(world).getFaction(id).removeSim(sim);
            }
        }



    }

    public WorkingController getController(){
        return controller;
    }

    public Activity getActivity() {
        if (activity == null){
        setActivity(Activity.IDLING);
    }
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setHouseID(UUID id){
        this.houseID = id;
    }

    public void removeHouse(){
        this.houseID = null;
    }

    public List<UUID> getOtherHouseOccupants(){
        ArrayList<UUID> list = SavedWorldData.get(world).getFactionWithSim(this.getUniqueID()).getOccupants(houseID);
        list.remove(this.getUniqueID());
        return list;

    }
    public void moveHouse(UUID currentHouseID,UUID newHouseID){
        Faction faction = SavedWorldData.get(world).getFactionWithSim(this.getUniqueID());
        faction.removeSimFromHouse(currentHouseID,this.getUniqueID());
        faction.addSimToHouse(newHouseID,getUniqueID());
        houseID = newHouseID;

    }

    public void findHouseToLive(){
        Faction faction = SavedWorldData.get(world).getFactionWithSim(this.getUniqueID());
        if (faction != null){
            UUID house = faction.getFreeHouse();
            if (house != null){
            faction.addSimToHouse(house,getUniqueID());
            faction.sendFactionChatMessage("Sim " + this.getName().getString() + " Has Moved into " + faction.getHouseByID(house).getName().replace("_"," "),world);

            houseID = house;
            }
        }
    }

    public boolean isHomeless(){
        return houseID == null;
    }
}