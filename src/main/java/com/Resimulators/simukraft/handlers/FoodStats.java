package com.Resimulators.simukraft.handlers;

import com.Resimulators.simukraft.common.entity.EntitySim;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FoodStats {
    private int foodLevel = 20;
    private float foodSaturationLevel = 5.0F;
    private float foodExhaustionLevel;
    private int foodTimer;
    private int prevFoodLevel = 20;

    public FoodStats() {
    }

    public void addStats(int foodLevel, float saturation) {
        this.foodLevel = Math.min(foodLevel + this.foodLevel, 20);
        this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)foodLevel * saturation * 2.0F, (float)this.foodLevel);
    }

    public void consume(Item item, ItemStack itemStack) {
        if (item.isFood()) {
            Food food = item.getFood();
            this.addStats(food.getHealing(), food.getSaturation());
        }

    }

    public void tick(EntitySim entitySim) {
        Difficulty difficulty = entitySim.world.getDifficulty();
        this.prevFoodLevel = this.foodLevel;
        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean naturalRegen = entitySim.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        if (naturalRegen && this.foodSaturationLevel > 0.0F && entitySim.shouldHeal() && this.foodLevel >= 20) {
            ++this.foodTimer;
            if (this.foodTimer >= 10) {
                float exhaustion = Math.min(this.foodSaturationLevel, 6.0F);
                entitySim.heal(exhaustion / 6.0F);
                this.addExhaustion(exhaustion);
                this.foodTimer = 0;
            }
        } else if (naturalRegen && this.foodLevel >= 18 && entitySim.shouldHeal()) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                entitySim.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
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
            this.foodLevel = compound.getInt("foodLevel");
            this.foodTimer = compound.getInt("foodTickTimer");
            this.foodSaturationLevel = compound.getFloat("foodSaturationLevel");
            this.foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
        }

    }

    public void write(CompoundNBT compound) {
        compound.putInt("foodLevel", this.foodLevel);
        compound.putInt("foodTickTimer", this.foodTimer);
        compound.putFloat("foodSaturationLevel", this.foodSaturationLevel);
        compound.putFloat("foodExhaustionLevel", this.foodExhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean needFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float exhaustion) {
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + exhaustion, 40.0F);
    }

    public float getSaturationLevel() {
        return this.foodSaturationLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public void setFoodSaturationLevel(float foodSaturationLevel) {
        this.foodSaturationLevel = foodSaturationLevel;
    }
}
