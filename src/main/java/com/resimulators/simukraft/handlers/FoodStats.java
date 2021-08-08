package com.resimulators.simukraft.handlers;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class FoodStats {
    private final SimEntity sim;
    private float foodExhaustionLevel;
    private int foodTimer;
    private int prevFoodLevel = 20;

    public FoodStats(SimEntity sim) {
        this.sim = sim;
    }

    public void consume(Item item, ItemStack itemStack) {
        if (item.isEdible()) {
            Food food = item.getFoodProperties();
            this.addStats(food.getNutrition(), food.getSaturationModifier());
        }

    }

    public void addStats(int foodLevel, float saturation) {
        this.setFoodLevel(Math.min(foodLevel + this.getFoodLevel(), 20));
        this.setFoodSaturationLevel(Math.min(this.getSaturationLevel() + this.getFoodLevel() * saturation * 2.0F, this.getFoodLevel()));
    }

    public int getFoodLevel() {
        return sim.getEntityData().get(SimEntity.FOOD_LEVEL);
    }

    public void setFoodLevel(int foodLevel) {
        sim.getEntityData().set(SimEntity.FOOD_LEVEL, foodLevel);
    }

    public void setFoodSaturationLevel(float foodSaturationLevel) {
        sim.getEntityData().set(SimEntity.FOOD_SATURATION_LEVEL, foodSaturationLevel);
    }

    public float getSaturationLevel() {
        return sim.getEntityData().get(SimEntity.FOOD_SATURATION_LEVEL);
    }

    public void tick(SimEntity simEntity) {
        Difficulty difficulty = simEntity.level.getDifficulty();
        this.prevFoodLevel = this.getFoodLevel();
        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.getSaturationLevel() > 0.0F) {
                this.setFoodSaturationLevel(Math.max(this.getSaturationLevel() - 1.0F, 0.0F));
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.setFoodLevel(Math.max(this.getFoodLevel() - 1, 0));
            }
        }

        boolean naturalRegen = simEntity.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (naturalRegen && this.getSaturationLevel() > 0.0F && simEntity.shouldHeal() && this.getFoodLevel() >= 20) {
            ++this.foodTimer;
            if (this.foodTimer >= 10) {
                float exhaustion = Math.min(this.getSaturationLevel(), 6.0F);
                simEntity.heal(exhaustion / 6.0F);
                this.addExhaustion(exhaustion);
                this.foodTimer = 0;
            }
        } else if (naturalRegen && this.getFoodLevel() >= 18 && simEntity.shouldHeal()) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                simEntity.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.getFoodLevel() <= 0) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                if (simEntity.getHealth() > 10.0F || difficulty == Difficulty.HARD || simEntity.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    simEntity.hurt(DamageSource.STARVE, 1.0F);
                }

                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }

    }

    public void addExhaustion(float exhaustion) {
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + exhaustion, 40.0F);
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

    public boolean needFood() {
        return sim.getEntityData().get(SimEntity.FOOD_LEVEL) < 20;
    }

    public boolean shouldEat() {
        return sim.getEntityData().get(SimEntity.FOOD_LEVEL) < 15;
    }


    public enum FoodLevels {
        LOW(3, "Starving"),
        MEDIUM(8, "Peckish"),
        HIGH(13, "A Little hungry"),
        FULL(18, "Full");


        public int level;
        public String status;

        FoodLevels(int level, String status) {
            this.level = level;
            this.status = status;

        }


    }
}
