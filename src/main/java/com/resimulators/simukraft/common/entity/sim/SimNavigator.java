package com.resimulators.simukraft.common.entity.sim;

import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.pathfinding.CustomWalkNodeProcessor;
import com.resimulators.simukraft.utils.BlockUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;

public class SimNavigator extends GroundPathNavigator {

    public SimNavigator(MobEntity p_i45875_1_, World p_i45875_2_) {
        super(p_i45875_1_, p_i45875_2_);

    }

    @Override
    public boolean moveTo(@Nullable Path path, double p_75484_2_) {
        this.doStuckDetection(this.getTempMobPos());
        if ((path != null && path.getDistToTarget() > SimuKraft.config.getSims().teleportDistance.get()) || this.isStuck()) {
            //teleport
            BlockPos teleportPosition = getTargetPos();
            ArrayList<BlockPos> blocks = BlockUtils.getBlocksAroundPosition(teleportPosition,1);
            if(!BlockUtils.blocksAreValid(level,blocks)){
                int radius = 5;
                blocks = BlockUtils.getBlocksAroundPosition(teleportPosition,radius);
                for (BlockPos pos : blocks){
                    ArrayList<BlockPos> innerBlocks = BlockUtils.getBlocksAroundPosition(pos,1);
                    innerBlocks.sort(Comparator.comparingDouble((block) -> getTargetPos().distSqr(block.getX(),block.getY(),block.getZ(),false)));
                    if (BlockUtils.blocksAreValid(level,innerBlocks)){
                        teleportPosition = pos;
                        break;
                    }

                }
            }
            mob.setPos(teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ());
            recomputePath();
            return true;
        } else {
            if (path == null || !path.sameAs(this.path)) {
                return super.moveTo(path, p_75484_2_);
            } else return false;
        }
    }

    @Override
    protected PathFinder createPathFinder(int p_179679_1_) {
        this.nodeEvaluator = new CustomWalkNodeProcessor();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, p_179679_1_);
    }

    @Override
    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vector3d vector3d = this.getTempMobPos();
                Vector3d vector3d1 = this.path.getNextEntityPos(this.mob);
                if (vector3d.y > vector3d1.y && !this.mob.isOnGround() && MathHelper.floor(vector3d.x) == MathHelper.floor(vector3d1.x) && MathHelper.floor(vector3d.z) == MathHelper.floor(vector3d1.z)) {
                    this.path.advance();
                }
            }

            DebugPacketSender.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vector3d vector3d2 = this.path.getNextEntityPos(this.mob);
                BlockPos blockpos = new BlockPos(vector3d2);
                this.mob.getMoveControl().setWantedPosition(vector3d2.x, this.level.getBlockState(blockpos.below()).isAir() ? vector3d2.y : CustomWalkNodeProcessor.getFloorLevel(this.level, blockpos), vector3d2.z, this.speedModifier);
            }
        }
    }
}
