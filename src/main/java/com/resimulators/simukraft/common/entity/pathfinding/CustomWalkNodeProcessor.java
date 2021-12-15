package com.resimulators.simukraft.common.entity.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CustomWalkNodeProcessor extends WalkNodeProcessor {


    public CustomWalkNodeProcessor(){
        super();
    }
    protected float oldWaterCost;
    private final Long2ObjectMap<PathNodeType> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
    private final Object2BooleanMap<AxisAlignedBB> collisionCache = new Object2BooleanOpenHashMap<>();

    @Override
    public void prepare(Region p_225578_1_, MobEntity p_225578_2_) {
        super.prepare(p_225578_1_, p_225578_2_);
        this.oldWaterCost = p_225578_2_.getPathfindingMalus(PathNodeType.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
        this.pathTypesByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public PathPoint getStart() {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        int i = MathHelper.floor(this.mob.getY());
        BlockState blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(blockstate.getFluidState().getType())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while(true) {
                    if (blockstate.getBlock() != Blocks.WATER && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                        --i;
                        break;
                    }

                    ++i;
                    blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
                }
            } else if (this.mob.isOnGround()) {
                i = MathHelper.floor(this.mob.getY() + 0.5D);
            } else {
                BlockPos blockpos;
                for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathType.LAND)) && blockpos.getY() > 0; blockpos = blockpos.below()) {
                }

                i = blockpos.above().getY();
            }
        } else {
            while(this.mob.canStandOnFluid(blockstate.getFluidState().getType())) {
                ++i;
                blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
            }

            --i;
        }

        BlockPos blockpos1 = this.mob.blockPosition();
        PathNodeType pathnodetype = this.getCachedBlockType(this.mob, blockpos1.getX(), i, blockpos1.getZ());
        if (this.mob.getPathfindingMalus(pathnodetype) < 0.0F) {
            AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();
            if (this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.minX, (double)i, axisalignedbb.minZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.minX, (double)i, axisalignedbb.maxZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.maxX, (double)i, axisalignedbb.minZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.maxX, (double)i, axisalignedbb.maxZ))) {
                PathPoint pathpoint = this.getNode(blockpos$mutable);
                pathpoint.type = this.getBlockPathType(this.mob, pathpoint.asBlockPos());
                pathpoint.costMalus = this.mob.getPathfindingMalus(pathpoint.type);
                return pathpoint;
            }
        }

        PathPoint pathpoint1 = this.getNode(blockpos1.getX(), i, blockpos1.getZ());
        pathpoint1.type = this.getBlockPathType(this.mob, pathpoint1.asBlockPos());
        pathpoint1.costMalus = this.mob.getPathfindingMalus(pathpoint1.type);
        return pathpoint1;
    }

    private boolean hasPositiveMalus(BlockPos p_237239_1_) {
        PathNodeType pathnodetype = this.getBlockPathType(this.mob, p_237239_1_);
        return this.mob.getPathfindingMalus(pathnodetype) >= 0.0F;
    }

    @Override
    public FlaggedPathPoint getGoal(double p_224768_1_, double p_224768_3_, double p_224768_5_) {
        return new FlaggedPathPoint(this.getNode(MathHelper.floor(p_224768_1_), MathHelper.floor(p_224768_3_), MathHelper.floor(p_224768_5_)));
    }

    @Override
    public int getNeighbors(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
        int i = 0;
        int j = 0;
        PathNodeType pathnodetype = this.getCachedBlockType(this.mob, p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z);
        PathNodeType pathnodetype1 = this.getCachedBlockType(this.mob, p_222859_2_.x, p_222859_2_.y, p_222859_2_.z);
        if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F && pathnodetype1 != PathNodeType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0F, this.mob.maxUpStep));
        }

        double d0 = getFloorLevel(this.level, new BlockPos(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z));
        PathPoint pathpoint = this.getLandNode(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
        if (this.isNeighborValid(pathpoint, p_222859_2_)) {
            p_222859_1_[i++] = pathpoint;
        }

        PathPoint pathpoint1 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z, j, d0, Direction.WEST, pathnodetype1);
        if (this.isNeighborValid(pathpoint1, p_222859_2_)) {
            p_222859_1_[i++] = pathpoint1;
        }

        PathPoint pathpoint2 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z, j, d0, Direction.EAST, pathnodetype1);
        if (this.isNeighborValid(pathpoint2, p_222859_2_)) {
            p_222859_1_[i++] = pathpoint2;
        }

        PathPoint pathpoint3 = this.getLandNode(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
        if (this.isNeighborValid(pathpoint3, p_222859_2_)) {
            p_222859_1_[i++] = pathpoint3;
        }

        PathPoint pathpoint4 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
        if (this.isDiagonalValid(p_222859_2_, pathpoint1, pathpoint3, pathpoint4)) {
            p_222859_1_[i++] = pathpoint4;
        }

        PathPoint pathpoint5 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
        if (this.isDiagonalValid(p_222859_2_, pathpoint2, pathpoint3, pathpoint5)) {
            p_222859_1_[i++] = pathpoint5;
        }

        PathPoint pathpoint6 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
        if (this.isDiagonalValid(p_222859_2_, pathpoint1, pathpoint, pathpoint6)) {
            p_222859_1_[i++] = pathpoint6;
        }

        PathPoint pathpoint7 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
        if (this.isDiagonalValid(p_222859_2_, pathpoint2, pathpoint, pathpoint7)) {
            p_222859_1_[i++] = pathpoint7;
        }

        return i;
    }

    private boolean isNeighborValid(PathPoint p_237235_1_, PathPoint p_237235_2_) {
        return p_237235_1_ != null && !p_237235_1_.closed && (p_237235_1_.costMalus >= 0.0F || p_237235_2_.costMalus < 0.0F);
    }

    private boolean isDiagonalValid(PathPoint p_222860_1_, @Nullable PathPoint p_222860_2_, @Nullable PathPoint p_222860_3_, @Nullable PathPoint p_222860_4_) {
        if (p_222860_4_ != null && p_222860_3_ != null && p_222860_2_ != null) {
            if (p_222860_4_.closed) {
                return false;
            } else if (p_222860_3_.y <= p_222860_1_.y && p_222860_2_.y <= p_222860_1_.y) {
                if (p_222860_2_.type != PathNodeType.WALKABLE_DOOR && p_222860_3_.type != PathNodeType.WALKABLE_DOOR && p_222860_4_.type != PathNodeType.WALKABLE_DOOR) {
                    boolean flag = p_222860_3_.type == PathNodeType.FENCE && p_222860_2_.type == PathNodeType.FENCE && (double)this.mob.getBbWidth() < 0.5D;
                    return p_222860_4_.costMalus >= 0.0F && (p_222860_3_.y < p_222860_1_.y || p_222860_3_.costMalus >= 0.0F || flag) && (p_222860_2_.y < p_222860_1_.y || p_222860_2_.costMalus >= 0.0F || flag);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean canReachWithoutCollision(PathPoint p_237234_1_) {
        Vector3d vector3d = new Vector3d((double)p_237234_1_.x - this.mob.getX(), (double)p_237234_1_.y - this.mob.getY(), (double)p_237234_1_.z - this.mob.getZ());
        AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();
        int i = MathHelper.ceil(vector3d.length() / axisalignedbb.getSize());
        vector3d = vector3d.scale((double)(1.0F / (float)i));

        for(int j = 1; j <= i; ++j) {
            axisalignedbb = axisalignedbb.move(vector3d);
            if (this.hasCollisions(axisalignedbb)) {
                return false;
            }
        }

        return true;
    }

    public static double getFloorLevel(IBlockReader p_197682_0_, BlockPos p_197682_1_) {
        BlockPos blockpos = p_197682_1_.below();
        VoxelShape voxelshape = p_197682_0_.getBlockState(blockpos).getCollisionShape(p_197682_0_, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
    }

    @Nullable
    private PathPoint getLandNode(int p_186332_1_, int p_186332_2_, int p_186332_3_, int p_186332_4_, double p_186332_5_, Direction p_186332_7_, PathNodeType p_186332_8_) {
        PathPoint pathpoint = null;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        double d0 = getFloorLevel(this.level, blockpos$mutable.set(p_186332_1_, p_186332_2_, p_186332_3_));
        if (d0 - p_186332_5_ > 1.125D) {
            return null;
        } else {
            PathNodeType pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
            float f = this.mob.getPathfindingMalus(pathnodetype);
            double d1 = (double)this.mob.getBbWidth() / 2.0D;
            if (f >= 0.0F) {
                pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                pathpoint.type = pathnodetype;
                pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
            }

            if (p_186332_8_ == PathNodeType.FENCE && pathpoint != null && pathpoint.costMalus >= 0.0F && !this.canReachWithoutCollision(pathpoint)) {
                pathpoint = null;
            }

            if (pathnodetype == PathNodeType.WALKABLE) {
                return pathpoint;
            } else {
                if ((pathpoint == null || pathpoint.costMalus < 0.0F) && p_186332_4_ > 0 && pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.UNPASSABLE_RAIL && pathnodetype != PathNodeType.TRAPDOOR) {
                    pathpoint = this.getLandNode(p_186332_1_, p_186332_2_ + 1, p_186332_3_, p_186332_4_ - 1, p_186332_5_, p_186332_7_, p_186332_8_);
                    if (pathpoint != null && (pathpoint.type == PathNodeType.OPEN || pathpoint.type == PathNodeType.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                        double d2 = (double)(p_186332_1_ - p_186332_7_.getStepX()) + 0.5D;
                        double d3 = (double)(p_186332_3_ - p_186332_7_.getStepZ()) + 0.5D;
                        AxisAlignedBB axisalignedbb = new AxisAlignedBB(d2 - d1, getFloorLevel(this.level, blockpos$mutable.set(d2, (double)(p_186332_2_ + 1), d3)) + 0.001D, d3 - d1, d2 + d1, (double)this.mob.getBbHeight() + getFloorLevel(this.level, blockpos$mutable.set((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z)) - 0.002D, d3 + d1);
                        if (this.hasCollisions(axisalignedbb)) {
                            pathpoint = null;
                        }
                    }
                }

                if (pathnodetype == PathNodeType.WATER && !this.canFloat()) {
                    if (this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_ - 1, p_186332_3_) != PathNodeType.WATER) {
                        return pathpoint;
                    }

                    while(p_186332_2_ > 0) {
                        --p_186332_2_;
                        pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
                        if (pathnodetype != PathNodeType.WATER) {
                            return pathpoint;
                        }

                        pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                        pathpoint.type = pathnodetype;
                        pathpoint.costMalus = Math.max(pathpoint.costMalus, this.mob.getPathfindingMalus(pathnodetype));
                    }
                }

                if (pathnodetype == PathNodeType.OPEN) {
                    int j = 0;
                    int i = p_186332_2_;

                    while(pathnodetype == PathNodeType.OPEN) {
                        --p_186332_2_;
                        if (p_186332_2_ < 0) {
                            PathPoint pathpoint3 = this.getNode(p_186332_1_, i, p_186332_3_);
                            pathpoint3.type = PathNodeType.BLOCKED;
                            pathpoint3.costMalus = -1.0F;
                            return pathpoint3;
                        }

                        if (j++ >= this.mob.getMaxFallDistance()) {
                            PathPoint pathpoint2 = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                            pathpoint2.type = PathNodeType.BLOCKED;
                            pathpoint2.costMalus = -1.0F;
                            return pathpoint2;
                        }

                        pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
                        f = this.mob.getPathfindingMalus(pathnodetype);
                        if (pathnodetype != PathNodeType.OPEN && f >= 0.0F) {
                            pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                            pathpoint.type = pathnodetype;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                            break;
                        }

                        if (f < 0.0F) {
                            PathPoint pathpoint1 = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                            pathpoint1.type = PathNodeType.BLOCKED;
                            pathpoint1.costMalus = -1.0F;
                            return pathpoint1;
                        }
                    }
                }

                if (pathnodetype == PathNodeType.FENCE) {
                    pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                    pathpoint.closed = true;
                    pathpoint.type = pathnodetype;
                    pathpoint.costMalus = pathnodetype.getMalus();
                }

                return pathpoint;
            }
        }
    }

    private boolean hasCollisions(AxisAlignedBB p_237236_1_) {
        return this.collisionCache.computeIfAbsent(p_237236_1_, (p_237237_2_) -> {
            return !this.level.noCollision(this.mob, p_237236_1_);
        });
    }

    @Override
    protected PathNodeType evaluateBlockPathType(IBlockReader p_215744_1_, boolean p_215744_2_, boolean p_215744_3_, BlockPos p_215744_4_, PathNodeType p_215744_5_) {
        if (p_215744_5_ == PathNodeType.DOOR_WOOD_CLOSED && p_215744_2_ && p_215744_3_) {
            p_215744_5_ = PathNodeType.WALKABLE_DOOR;
        }

        if (p_215744_5_ == PathNodeType.DOOR_OPEN && !p_215744_3_) {
            p_215744_5_ = PathNodeType.BLOCKED;
        }

        if (p_215744_5_ == PathNodeType.RAIL && !(p_215744_1_.getBlockState(p_215744_4_).getBlock() instanceof AbstractRailBlock) && !(p_215744_1_.getBlockState(p_215744_4_.below()).getBlock() instanceof AbstractRailBlock)) {
            p_215744_5_ = PathNodeType.UNPASSABLE_RAIL;
        }

        if (p_215744_5_ == PathNodeType.LEAVES) {
            p_215744_5_ = PathNodeType.BLOCKED;
        }

        return p_215744_5_;
    }

    private PathNodeType getBlockPathType(MobEntity p_186329_1_, BlockPos p_186329_2_) {
        return this.getCachedBlockType(p_186329_1_, p_186329_2_.getX(), p_186329_2_.getY(), p_186329_2_.getZ());
    }

    private PathNodeType getCachedBlockType(MobEntity p_237230_1_, int p_237230_2_, int p_237230_3_, int p_237230_4_) {
        return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(p_237230_2_, p_237230_3_, p_237230_4_), (p_237229_5_) -> {
            return this.getBlockPathType(this.level, p_237230_2_, p_237230_3_, p_237230_4_, p_237230_1_, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
        });
    }

    public static PathNodeType checkNeighbourBlocks(IBlockReader p_237232_0_, BlockPos.Mutable p_237232_1_, PathNodeType p_237232_2_) {
        int i = p_237232_1_.getX();
        int j = p_237232_1_.getY();
        int k = p_237232_1_.getZ();

        for(int l = -1; l <= 1; ++l) {
            for(int i1 = -1; i1 <= 1; ++i1) {
                for(int j1 = -1; j1 <= 1; ++j1) {
                    if (l != 0 || j1 != 0) {
                        p_237232_1_.set(i + l, j + i1, k + j1);
                        BlockState blockstate = p_237232_0_.getBlockState(p_237232_1_);
                        if (blockstate.is(Blocks.CACTUS)) {
                            return PathNodeType.DANGER_CACTUS;
                        }

                        if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                            return PathNodeType.DANGER_OTHER;
                        }

                        if (isBurningBlock(blockstate)) {
                            return PathNodeType.DANGER_FIRE;
                        }

                        if (p_237232_0_.getFluidState(p_237232_1_).is(FluidTags.WATER)) {
                            return PathNodeType.WATER_BORDER;
                        }
                    }
                }
            }
        }

        return p_237232_2_;
    }

    @Override
    public PathNodeType getBlockPathType(IBlockReader p_186319_1_, int p_186319_2_, int p_186319_3_, int p_186319_4_, MobEntity p_186319_5_, int p_186319_6_, int p_186319_7_, int p_186319_8_, boolean p_186319_9_, boolean p_186319_10_) {
        EnumSet<PathNodeType> enumset = EnumSet.noneOf(PathNodeType.class);
        PathNodeType pathnodetype = PathNodeType.BLOCKED;
        BlockPos blockpos = p_186319_5_.blockPosition();
        pathnodetype = this.getBlockPathTypes(p_186319_1_, p_186319_2_, p_186319_3_, p_186319_4_, p_186319_6_, p_186319_7_, p_186319_8_, p_186319_9_, p_186319_10_, enumset, pathnodetype, blockpos);
        if (enumset.contains(PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        } else if (enumset.contains(PathNodeType.UNPASSABLE_RAIL)) {
            return PathNodeType.UNPASSABLE_RAIL;
        } else {
            PathNodeType pathnodetype1 = PathNodeType.BLOCKED;

            for(PathNodeType pathnodetype2 : enumset) {
                if (p_186319_5_.getPathfindingMalus(pathnodetype2) < 0.0F) {
                    return pathnodetype2;
                }

                if (p_186319_5_.getPathfindingMalus(pathnodetype2) >= p_186319_5_.getPathfindingMalus(pathnodetype1)) {
                    pathnodetype1 = pathnodetype2;
                }
            }

            return pathnodetype == PathNodeType.OPEN && p_186319_5_.getPathfindingMalus(pathnodetype1) == 0.0F && p_186319_6_ <= 1 ? PathNodeType.OPEN : pathnodetype1;
        }
    }

    @Override
    public PathNodeType getBlockPathTypes(IBlockReader p_193577_1_, int p_193577_2_, int p_193577_3_, int p_193577_4_, int p_193577_5_, int p_193577_6_, int p_193577_7_, boolean p_193577_8_, boolean p_193577_9_, EnumSet<PathNodeType> p_193577_10_, PathNodeType p_193577_11_, BlockPos p_193577_12_) {
        for(int i = 0; i < p_193577_5_; ++i) {
            for(int j = 0; j < p_193577_6_; ++j) {
                for(int k = 0; k < p_193577_7_; ++k) {
                    int l = i + p_193577_2_;
                    int i1 = j + p_193577_3_;
                    int j1 = k + p_193577_4_;
                    PathNodeType pathnodetype = this.getBlockPathType(p_193577_1_, l, i1, j1);
                    pathnodetype = this.evaluateBlockPathType(p_193577_1_, p_193577_8_, p_193577_9_, p_193577_12_, pathnodetype);
                    if (i == 0 && j == 0 && k == 0) {
                        p_193577_11_ = pathnodetype;
                    }

                    p_193577_10_.add(pathnodetype);
                }
            }
        }

        return p_193577_11_;
    }

    @Override
    public PathNodeType getBlockPathType(IBlockReader blockReader, int xpos, int ypos, int zpos) {
        return getBlockPathTypeStatic(blockReader, new BlockPos.Mutable(xpos, ypos, zpos));
    }

    public static PathNodeType getBlockPathTypeStatic(IBlockReader blockReader, BlockPos.Mutable blockpos) {
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
        PathNodeType pathnodetype = getBlockPathTypeRaw(blockReader, blockpos);
        if (pathnodetype == PathNodeType.OPEN && j >= 1) {
            PathNodeType pathnodetype1 = getBlockPathTypeRaw(blockReader, blockpos.set(i, j - 1, k));
            pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
            if (pathnodetype1 == PathNodeType.DAMAGE_FIRE) {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
                pathnodetype = PathNodeType.DAMAGE_CACTUS;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
                pathnodetype = PathNodeType.DAMAGE_OTHER;
            }

            if (pathnodetype1 == PathNodeType.STICKY_HONEY) {
                pathnodetype = PathNodeType.STICKY_HONEY;
            }
        }

        if (pathnodetype == PathNodeType.WALKABLE) {
            pathnodetype = checkNeighbourBlocks(blockReader, blockpos.set(i, j, k), pathnodetype);
        }

        return pathnodetype;
    }

    public static PathNodeType getBlockPathTypeRaw(IBlockReader blockReader, BlockPos blockPos) {
        BlockState blockstate = blockReader.getBlockState(blockPos);
        PathNodeType type = blockstate.getAiPathNodeType(blockReader, blockPos);
        if (type != null) return type;
        Block block = blockstate.getBlock();
        Material material = blockstate.getMaterial();
        if (blockstate.isAir(blockReader, blockPos)) {
            return PathNodeType.OPEN;
        } else if (!blockstate.is(BlockTags.TRAPDOORS) && !blockstate.is(Blocks.LILY_PAD)) {
            if (blockstate.is(Blocks.CACTUS)) {
                return PathNodeType.DAMAGE_CACTUS;
            } else if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                return PathNodeType.DAMAGE_OTHER;
            } else if (blockstate.is(Blocks.HONEY_BLOCK)) {
                return PathNodeType.STICKY_HONEY;
            } else if (blockstate.is(Blocks.COCOA)) {
                return PathNodeType.COCOA;
            } else {
                FluidState fluidstate = blockReader.getFluidState(blockPos);
                if (fluidstate.is(FluidTags.WATER)) {
                    return PathNodeType.WATER;
                } else if (fluidstate.is(FluidTags.LAVA)) {
                    return PathNodeType.LAVA;
                } else if (isBurningBlock(blockstate)) {
                    return PathNodeType.DAMAGE_FIRE;
                } else if (DoorBlock.isWoodenDoor(blockstate) && !blockstate.getValue(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_WOOD_CLOSED;
                } else if (block instanceof DoorBlock && material == Material.METAL && !blockstate.getValue(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_IRON_CLOSED;
                } else if (block instanceof DoorBlock && blockstate.getValue(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_OPEN;
                } else if (block instanceof AbstractRailBlock) {
                    return PathNodeType.RAIL;
                } else if (block instanceof LeavesBlock) {
                    return PathNodeType.LEAVES;
                } else if (!block.is(BlockTags.FENCES) && !block.is(BlockTags.WALLS) && !(block instanceof FenceGateBlock)) { // removed check for gate is open as ai is supposed to open the gate
                    return !blockstate.isPathfindable(blockReader, blockPos, PathType.LAND) ? PathNodeType.BLOCKED : PathNodeType.OPEN;
                } else if (block instanceof FenceGateBlock) {
                    return PathNodeType.WALKABLE_DOOR;
                }else {
                    return PathNodeType.FENCE;
                }
            }
        } else {
            return PathNodeType.TRAPDOOR;
        }
    }

    private static boolean isBurningBlock(BlockState blockState) {
        return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }
}
