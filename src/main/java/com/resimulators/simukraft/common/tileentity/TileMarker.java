package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;


public class TileMarker extends TileEntity {


    private BlockPos origin;
    private BlockPos backLeft;
    private BlockPos frontRight;
    private BlockPos backRight;
    boolean used = false;
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
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        if (nbt.contains("origin")) origin = BlockPos.fromLong(nbt.getLong("origin"));
        if (nbt.contains("backleft")) backLeft = BlockPos.fromLong(nbt.getLong("backleft"));
        if (nbt.contains("frontright")) frontRight = BlockPos.fromLong(nbt.getLong("frontright"));
        if (nbt.contains("backright")) backRight = BlockPos.fromLong(nbt.getLong("backright"));
        if (nbt.contains("used")) used = nbt.getBoolean("used");
        if (nbt.contains("range")) range = nbt.getInt("range");
        if (nbt.contains("corner")) corner = Corner.fromNbt(nbt.getInt("corner"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        if (origin != null) nbt.putLong("origin",origin.toLong());
        if (backLeft != null) nbt.putLong("backleft", backLeft.toLong());
        if (frontRight != null) nbt.putLong("frontright",frontRight.toLong());
        if (backRight != null) nbt.putLong("backright",backRight.toLong());
        nbt.putBoolean("used",used);
        nbt.putInt("range",range);
        if (corner != null) nbt.putInt("corner",Corner.toNbt(corner));
        return super.write(nbt);
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB area = new AxisAlignedBB(this.pos);
        if (backRight != null) {
            area = new AxisAlignedBB(this.pos, backRight);
            return area;
        }else
        if (backLeft != null) {
            area = new AxisAlignedBB(this.pos, backLeft);
            return area;
        }else
        if (frontRight != null) {
            area = new AxisAlignedBB(this.pos, frontRight);
            return area;
        }
        return area;
    }


    public void onRightClick(Direction dir) {
        int x = 0;
        int z = 0;
        if (!used){
            used = true;
            origin = this.pos;

        for (int i = 1; i < range; i++) {
            BlockPos pos = this.pos.offset(dir, i);
            if (this.world.getTileEntity(pos) instanceof TileMarker) {
                TileMarker marker = (TileMarker) this.world.getTileEntity(pos);
                marker.used = true;
                setBackLeft(pos);
                marker.setOrigin(this.pos);
                marker.setCorner(Corner.BACKLEFT);
                x = i;
                break;
            }

        }

        for (int i = 1; i < range; i++) {
            BlockPos pos = this.pos.offset(dir.rotateY(), i);
            if (this.world.getTileEntity(pos) instanceof TileMarker) {
                TileMarker marker = (TileMarker) this.world.getTileEntity(pos);
                marker.used = true;
                setFrontRight(pos);
                marker.setOrigin(this.pos);
                marker.setCorner(Corner.FRONTRIGHT);

                z = i;
                break;
                }
            }
        if (x != 0 && z != 0) {backRight = this.pos.add(x,0,z);

        }
        markDirty();


        }if (backLeft != null){
        if (world.getTileEntity(backLeft) != null){
            TileMarker marker = (TileMarker) world.getTileEntity(backLeft);
            marker.setFrontRight(frontRight);
            marker.setBackLeft(backLeft);
            marker.setBackRight(backRight);
        }}
        if (frontRight != null){
        if (world.getTileEntity(frontRight) != null){
            TileMarker marker = (TileMarker) world.getTileEntity(frontRight);
            marker.setFrontRight(frontRight);
            marker.setBackLeft(backLeft);
            marker.setBackRight(backRight);
        }}

    }

    public void setBackLeft(BlockPos backLeft) {
        this.backLeft = backLeft;
        markDirty();
    }

    public void setBackRight(BlockPos backRight) {
        this.backRight = backRight;
        markDirty();
    }

    public void setFrontRight(BlockPos frontRight) {
        this.frontRight = frontRight;
        markDirty();
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
        markDirty();
    }

    public void setCorner(Corner corner) {
        this.corner = corner;
        markDirty();
    }
    public BlockPos getOrigin() {
        return origin;
    }

    public BlockPos getBackLeft() {
        return backLeft;
    }

    public BlockPos getFrontRight() {
        return frontRight;
    }

    public BlockPos getBackRight() {
        return backRight;
    }

    public boolean isUsed() {
        return used;
    }

    public Corner getCorner() {
        return corner;
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

    public void onDestroy(BlockPos pos){
        if (corner == Corner.ORIGIN) {
            checkcorners();
            if (frontRight != null){
                ((TileMarker)world.getTileEntity(frontRight)).checkcorners();
            }
            if (backLeft != null){
                if (world.getTileEntity(backLeft) != null) ((TileMarker)world.getTileEntity(backLeft)).checkcorners();
            }
        }else if (corner == Corner.BACKLEFT){
            checkcorners();
            if(origin != null){
                ((TileMarker)world.getTileEntity(origin)).checkcorners();
            }
            if (frontRight != null){
                ((TileMarker)world.getTileEntity(frontRight)).checkcorners();
            }
        } else if (corner == Corner.FRONTRIGHT){
            checkcorners();
            if(origin != null){
                ((TileMarker)world.getTileEntity(origin)).checkcorners();
            }
            if (frontRight != null){
                ((TileMarker)world.getTileEntity(frontRight)).checkcorners();
            }
    }
    }

    private void checkcorners(){
        if (frontRight != null){
            if(world.getTileEntity(frontRight) == null ){
            setFrontRight(null);}
        }
        if (backLeft != null){
        if (world.getTileEntity(backLeft) == null){
            setBackLeft(null);}
        }
        if (origin != null){
        if (world.getTileEntity(origin) == null){
           setOrigin(null);
        }
        }
        markDirty();
    }



    public enum Corner {
        ORIGIN,
        BACKLEFT,
        BACKRIGHT,
        FRONTRIGHT;

        public static int toNbt(Corner corner){
            switch(corner){
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
        public static Corner fromNbt(int num){
            switch (num){
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
