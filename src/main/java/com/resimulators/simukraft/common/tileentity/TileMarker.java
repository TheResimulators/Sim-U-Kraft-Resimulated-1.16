package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;


public class TileMarker extends TileEntity {
    boolean used = false;
    private BlockPos origin;
    private BlockPos backLeft;
    private BlockPos frontRight;
    private BlockPos backRight;
    private Corner corner = Corner.ORIGIN;
    private int range = 40;


    public TileMarker() {
        super(ModTileEntities.MARKER.get());
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB area = new AxisAlignedBB(this.worldPosition);
        if (backRight != null) {
            area = new AxisAlignedBB(this.worldPosition, backRight);
            return area;
        } else if (backLeft != null) {
            area = new AxisAlignedBB(this.worldPosition, backLeft);
            return area;
        } else if (frontRight != null) {
            area = new AxisAlignedBB(this.worldPosition, frontRight);
            return area;
        }
        return area;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains("origin")) origin = BlockPos.of(nbt.getLong("origin"));
        if (nbt.contains("backleft")) backLeft = BlockPos.of(nbt.getLong("backleft"));
        if (nbt.contains("frontright")) frontRight = BlockPos.of(nbt.getLong("frontright"));
        if (nbt.contains("backright")) backRight = BlockPos.of(nbt.getLong("backright"));
        if (nbt.contains("used")) used = nbt.getBoolean("used");
        if (nbt.contains("range")) range = nbt.getInt("range");
        if (nbt.contains("corner")) corner = Corner.fromNbt(nbt.getInt("corner"));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if (origin != null) nbt.putLong("origin", origin.asLong());
        if (backLeft != null) nbt.putLong("backleft", backLeft.asLong());
        if (frontRight != null) nbt.putLong("frontright", frontRight.asLong());
        if (backRight != null) nbt.putLong("backright", backRight.asLong());
        nbt.putBoolean("used", used);
        nbt.putInt("range", range);
        if (corner != null) nbt.putInt("corner", Corner.toNbt(corner));
        return super.save(nbt);
    }

    public void onRightClick(Direction dir) {
        int x = 0;
        int z = 0;
        if (!used) {
            used = true;
            origin = this.worldPosition;

            for (int i = 1; i < range; i++) {
                BlockPos pos = this.worldPosition.relative(dir, i);
                if (this.level.getBlockEntity(pos) instanceof TileMarker) {
                    TileMarker marker = (TileMarker) this.level.getBlockEntity(pos);
                    marker.used = true;
                    setBackLeft(pos);
                    marker.setOrigin(this.worldPosition);
                    marker.setCorner(Corner.BACKLEFT);
                    x = i;
                    break;
                }

            }

            for (int i = 1; i < range; i++) {
                BlockPos pos = this.worldPosition.relative(dir.getClockWise(), i);
                if (this.level.getBlockEntity(pos) instanceof TileMarker) {
                    TileMarker marker = (TileMarker) this.level.getBlockEntity(pos);
                    marker.used = true;
                    setFrontRight(pos);
                    marker.setOrigin(this.worldPosition);
                    marker.setCorner(Corner.FRONTRIGHT);

                    z = i;
                    break;
                }
            }
            if (x != 0 && z != 0) {
                backRight = this.worldPosition.offset(x, 0, z);

            }
            setChanged();


        }
        if (backLeft != null) {
            if (level.getBlockEntity(backLeft) != null) {
                TileMarker marker = (TileMarker) level.getBlockEntity(backLeft);
                marker.setFrontRight(frontRight);
                marker.setBackLeft(backLeft);
                marker.setBackRight(backRight);
            }
        }
        if (frontRight != null) {
            if (level.getBlockEntity(frontRight) != null) {
                TileMarker marker = (TileMarker) level.getBlockEntity(frontRight);
                marker.setFrontRight(frontRight);
                marker.setBackLeft(backLeft);
                marker.setBackRight(backRight);
            }
        }

    }

    public Direction getOrientation() {
        if (backLeft != null && origin != null) {
            if (backLeft.getX() > origin.getX())
                return Direction.EAST;
            else if (backLeft.getX() < origin.getX())
                return Direction.WEST;
            if (backLeft.getZ() > origin.getZ())
                return Direction.SOUTH;
            else if (backLeft.getZ() < origin.getZ())
                return Direction.NORTH;
        }
        return null;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
        setChanged();
    }

    public BlockPos getBackLeft() {
        return backLeft;
    }

    public void setBackLeft(BlockPos backLeft) {
        this.backLeft = backLeft;
        setChanged();
    }

    public BlockPos getFrontRight() {
        return frontRight;
    }

    public void setFrontRight(BlockPos frontRight) {
        this.frontRight = frontRight;
        setChanged();
    }

    public BlockPos getBackRight() {
        return backRight;
    }

    public void setBackRight(BlockPos backRight) {
        this.backRight = backRight;
        setChanged();
    }

    public boolean isUsed() {
        return used;
    }

    public Corner getCorner() {
        return corner;
    }

    public void setCorner(Corner corner) {
        this.corner = corner;
        setChanged();
    }

    public int getRange() {
        return range;
    }

    public Corner rotateCorner(Corner corner) {
        switch (corner) {
            case ORIGIN:
                return Corner.BACKLEFT;
            case BACKLEFT:
                return Corner.BACKRIGHT;
            case BACKRIGHT:
                return Corner.FRONTRIGHT;
            case FRONTRIGHT:
                return Corner.ORIGIN;
            default:
                return corner;
        }
    }

    public void onDestroy(BlockPos pos) {
        if (corner == Corner.ORIGIN) {
            checkcorners();
            if (frontRight != null) {
                ((TileMarker) level.getBlockEntity(frontRight)).checkcorners();
            }
            if (backLeft != null) {
                if (level.getBlockEntity(backLeft) != null)
                    ((TileMarker) level.getBlockEntity(backLeft)).checkcorners();
            }
        } else if (corner == Corner.BACKLEFT) {
            checkcorners();
            if (origin != null) {
                ((TileMarker) level.getBlockEntity(origin)).checkcorners();
            }
            if (frontRight != null) {
                ((TileMarker) level.getBlockEntity(frontRight)).checkcorners();
            }
        } else if (corner == Corner.FRONTRIGHT) {
            checkcorners();
            if (origin != null) {
                ((TileMarker) level.getBlockEntity(origin)).checkcorners();
            }
            if (frontRight != null) {
                ((TileMarker) level.getBlockEntity(frontRight)).checkcorners();
            }
        }
    }

    private void checkcorners() {
        if (frontRight != null) {
            if (level.getBlockEntity(frontRight) == null) {
                setFrontRight(null);
            }
        }
        if (backLeft != null) {
            if (level.getBlockEntity(backLeft) == null) {
                setBackLeft(null);
            }
        }
        if (origin != null) {
            if (level.getBlockEntity(origin) == null) {
                setOrigin(null);
            }
        }
        setChanged();
    }


    public enum Corner {
        ORIGIN,
        BACKLEFT,
        BACKRIGHT,
        FRONTRIGHT;

        public static int toNbt(Corner corner) {
            switch (corner) {
                case BACKLEFT:
                    return 1;
                case BACKRIGHT:
                    return 2;
                case FRONTRIGHT:
                    return 3;
                default:
                    return 0;
            }

        }

        public static Corner fromNbt(int num) {
            switch (num) {
                case 0:
                    return Corner.ORIGIN;
                case 1:
                    return Corner.BACKLEFT;
                case 2:
                    return Corner.BACKRIGHT;
                case 3:
                    return Corner.FRONTRIGHT;
            }
            return null;
        }
    }
}
