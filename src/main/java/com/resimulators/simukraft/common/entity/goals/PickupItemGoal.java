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
    private PathNavigator navigator;
    private ItemEntity item;

    public PickupItemGoal(SimEntity sim) {
        this.sim = sim;
        this.navigator = sim.getNavigator();
    }

    @Override
    public boolean shouldExecute() {
        if (!navigator.noPath())
            return false;

        if (sim.world != null) {
            List<ItemEntity> items = sim.world.getEntitiesWithinAABB(ItemEntity.class, sim.getBoundingBox().grow(10));
            ItemEntity closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (ItemEntity item : items) {
                if (item.isAlive() && item.isOnGround()) {
                    double distance = item.getDistance(sim);
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
    public void resetTask() {
        navigator.clearPath();
        this.item = null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return sim.isAlive() && !navigator.noPath() && this.item.isAlive();
    }

    @Override
    public void startExecuting() {
        if (this.item != null) {
            navigator.tryMoveToXYZ(this.item.getPosition().getX(), this.item.getPosition().getY(), this.item.getPosition().getZ(), 0.6f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!sim.world.isRemote) {
            if (this.item != null && sim.getDistance(this.item) < 1.5) {
                final ItemStack toPickup = this.item.getItem();
                final ItemStack rest = ItemHandlerHelper.insertItem(sim.getInventory().getHandler(), toPickup, false);
                if (rest.getCount() < toPickup.getCount()) {
                    Utils.setEntityItemStack(this.item, rest);
                }
            }
        }
    }
}
