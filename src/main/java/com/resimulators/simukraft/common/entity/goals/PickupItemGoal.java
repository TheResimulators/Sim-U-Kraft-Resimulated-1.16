package com.resimulators.simukraft.common.entity.goals;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.utils.Utils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class PickupItemGoal extends Goal {
    private final SimEntity sim;
    private final PathNavigator navigator;
    private ItemEntity item;

    public PickupItemGoal(SimEntity sim) {
        this.sim = sim;
        this.navigator = sim.getNavigation();
    }

    @Override
    public boolean canUse() {
        if (!navigator.isDone())
            return false;

        if (sim.level != null) {
            List<ItemEntity> items = sim.level.getEntitiesOfClass(ItemEntity.class, sim.getBoundingBox().inflate(10));
            ItemEntity closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (ItemEntity item : items) {
                if (item.isAlive() && item.isOnGround()) {
                    double distance = item.distanceTo(sim);
                    if (distance < closestDistance && sim.canPickupStack(item.getItem()) && !item.isInWater() && !item.isInLava()) {
                        closest = item;
                        closestDistance = distance;
                    }
                }
            }
            if (closest != null) {
                this.item = closest;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return sim.isAlive() && !navigator.isDone() && this.item.isAlive();
    }

    @Override
    public void start() {
        if (this.item != null) {
            navigator.moveTo(this.item.blockPosition().getX(), this.item.blockPosition().getY(), this.item.blockPosition().getZ(), 0.6f);
        }
    }

    @Override
    public void stop() {
        navigator.stop();
        this.item = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!sim.level.isClientSide) {
            if (this.item != null && sim.distanceTo(this.item) < 1.5) {
                final ItemStack toPickup = this.item.getItem();
                final ItemStack rest = ItemHandlerHelper.insertItem(sim.getInventory().getHandler(), toPickup, false);
                if (rest.getCount() < toPickup.getCount()) {
                    Utils.setEntityItemStack(this.item, rest);
                }
            }
        }
    }
}
