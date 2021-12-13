package com.resimulators.simukraft.common.entity.sim;

import com.resimulators.simukraft.SimuKraft;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SimNavigator extends GroundPathNavigator {

    public SimNavigator(MobEntity p_i45875_1_, World p_i45875_2_) {
        super(p_i45875_1_, p_i45875_2_);
    }

    @Override
    public boolean moveTo(@Nullable Path path, double p_75484_2_) {
        this.doStuckDetection(this.getTempMobPos());
        if ((path != null && path.getDistToTarget() > SimuKraft.config.getSims().teleportDistance.get()) || this.isStuck()) {
            //teleport
            mob.setPos(getTargetPos().getX(), getTargetPos().getY(), getTargetPos().getZ());
            return true;
        } else {
            if (path == null || !path.sameAs(this.path)) {
                return super.moveTo(path, p_75484_2_);
            } else return false;
        }
    }
}
