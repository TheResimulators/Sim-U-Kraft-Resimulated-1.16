package com.resimulators.simukraft.handlers;

import com.resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.resimulators.simukraft.common.entity.sim.EntitySim.FOOD_LEVEL;
import static com.resimulators.simukraft.common.entity.sim.EntitySim.FOOD_SATURATION_LEVEL;

public class FoodStats {
    private float foodExhaustionLevel;
    private int foodTimer;
    private int prevFoodLevel = 20;

    private EntitySim sim;

    public FoodStats(EntitySim sim) {
        this.sim = sim;
    }

    public void addStats(int foodLevel, float saturation) {
        this.setFoodLevel(Math.min(foodLevel + this.getFoodLevel(), 20));
        this.setFoodSaturationLevel(Math.min(this.getSaturationLevel() + this.getFoodLevel() * saturation * 2.0F, this.getFoodLevel()));
    }

    public void consume(Item item, ItemStack itemStack) {
        if (item.isFood()) {
            Food food = item.getFood();
            this.addStats(food.getHealing(), food.getSaturation());
        }

    }

    public void tick(EntitySim entitySim) {
        Difficulty difficulty = entitySim.world.getDifficulty();
        this.prevFoodLevel = this.getFoodLevel();
        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.getSaturationLevel() > 0.0F) {
                this.setFoodSaturationLevel(Math.max(this.getSaturationLevel() - 1.0F, 0.0F));
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.setFoodLevel(Math.max(this.getFoodLevel() - 1, 0));
            }
        }

        boolean naturalRegen = entitySim.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        if (naturalRegen && this.getSaturationLevel() > 0.0F && entitySim.shouldHeal() && this.getFoodLevel() >= 20) {
            ++this.foodTimer;
            if (this.foodTimer >= 10) {
                float exhaustion = Math.min(this.getSaturationLevel(), 6.0F);
                entitySim.heal(exhaustion / 6.0F);
                this.addExhaustion(exhaustion);
                this.foodTimer = 0;
            }
        } else if (naturalRegen && this.getFoodLevel() >= 18 && entitySim.shouldHeal()) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                entitySim.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.getFoodLevel() <= 0) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                if (entitySim.getHealth() > 10.0F || difficulty == Difficulty.HARD || entitySim.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    entitySim.attackEntityFrom(DamageSource.STARVE, 1.0F);
                }

                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }

    }

    public void read(CompoundNBT compound) {
        if (compound.contains("foodLevel", 99)) {
            this.setFoodLevel(compound.getInt("foodLevel"));
            this.foodTimer = compound.getInt("foodTickTimer");
            this.setFoodSaturationLevel(compound.getFloat("foodSaturationLevel"));
            this.foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
        }

    }

    public void write(CompoundNBT compound) {
        compound.putInt("foodLevel", this.getFoodLevel());
        compound.putInt("foodTickTimer", this.foodTimer);
        compound.putFloat("foodSaturationLevel", this.getSaturationLevel());
        compound.putFloat("foodExhaustionLevel", this.foodExhaustionLevel);
    }

    public int getFoodLevel() {
        return sim.getDataManager().get(FOOD_LEVEL);
    }

    public boolean needFood() {
        return sim.getDataManager().get(FOOD_LEVEL) < 20;
    }

    public boolean shouldEat() {
        return sim.getDataManager().get(FOOD_LEVEL) < 15;
    }

    public void addExhaustion(float exhaustion) {
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + exhaustion, 40.0F);
    }

    public float getSaturationLevel() {
        return sim.getDataManager().get(FOOD_SATURATION_LEVEL);
    }

    public void setFoodLevel(int foodLevel) {
        sim.getDataManager().set(FOOD_LEVEL, foodLevel);
    }

    @OnlyIn(Dist.CLIENT)
    public void setFoodSaturationLevel(float foodSaturationLevel) {
        sim.getDataManager().set(FOOD_SATURATION_LEVEL, foodSaturationLevel);
    }
}
