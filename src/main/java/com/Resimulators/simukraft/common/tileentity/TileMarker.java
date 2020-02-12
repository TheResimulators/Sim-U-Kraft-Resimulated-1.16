package com.Resimulators.simukraft.common.tileentity;

import com.Resimulators.simukraft.init.ModTileEntities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

enum Corner {
    ORIGIN,
    BACKLEFT,
    BACKRIGHT,
    FRONTRIGHT;

    public static int toNbt(Corner corner){
        switch(corner){
            case ORIGIN:
                return 0;
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

public class TileMarker extends TileEntity {
    private BlockPos origin;
    private BlockPos backLeft;
    private BlockPos frontRight;
    private BlockPos backRight;
    boolean used = false;
    private Corner corner = Corner.ORIGIN;
    private int range = 40;


    public TileMarker() {
        super(ModTileEntities.MARKER);
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public void read(CompoundNBT nbt) {
        origin = BlockPos.fromLong(nbt.getLong("origin"));
        backLeft = BlockPos.fromLong(nbt.getLong("backleft"));
        frontRight = BlockPos.fromLong(nbt.getLong("frontright"));
        backRight = BlockPos.fromLong(nbt.getLong("backright"));
        used = nbt.getBoolean("used");
        range = nbt.getInt("range");
        corner = Corner.fromNbt(nbt.getInt("corner"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putLong("origin",origin.toLong());
        nbt.putLong("backleft", backLeft.toLong());
        nbt.putLong("frontright",frontRight.toLong());
        nbt.putLong("backright",backRight.toLong());
        nbt.putBoolean("used",used);
        nbt.putInt("range",range);
        nbt.putInt("corner",Corner.toNbt(corner));
        return nbt;
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB area = new AxisAlignedBB(this.pos);
        if (backRight != null) {
            area = new AxisAlignedBB(this.pos, backRight);
        }
        if (backLeft != null) {
            area = new AxisAlignedBB(this.pos, backLeft);
        }
        if (frontRight != null) {
            area = new AxisAlignedBB(this.pos, frontRight);
        }
        return area;
    }


    public void onRightClick(Direction dir) {
        if (!used){
        for (int i = 0; i < range; i++) {
            BlockPos pos = this.pos.offset(dir, i);
            if (this.world.getTileEntity(pos) instanceof TileMarker) {
                TileMarker marker = (TileMarker) this.world.getTileEntity(pos);
                marker.used = true;
                setBackLeft(pos);
                backRight = backRight.offset(dir, i);
                marker.setOrigin(this.pos);
                marker.setCorner(Corner.BACKLEFT);
                break;
            }

        }

        for (int i = 0; i < range; i++) {
            BlockPos pos = this.pos.offset(dir.rotateY(), i);
            if (this.world.getTileEntity(pos) instanceof TileMarker) {
                TileMarker marker = (TileMarker) this.world.getTileEntity(pos);
                marker.used = true;
                setBackLeft(pos);
                backRight = backRight.offset(dir.rotateY(), i);
                marker.setOrigin(this.pos);
                marker.setCorner(Corner.FRONTRIGHT);

                break;
                }
            }
        markDirty();
        }
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
        if (corner != Corner.ORIGIN){
            TileMarker marker = (TileMarker) world.getTileEntity(origin);
            if (pos == marker.backLeft){
                marker.setBackLeft(null);
                marker.setBackRight(null);
            }else if(pos== marker.frontRight){
                marker.setFrontRight(null);
                marker.setBackRight(null);
            }
        }else{
            ((TileMarker)world.getTileEntity(frontRight)).setOrigin(null);
            ((TileMarker)world.getTileEntity(backLeft)).setOrigin(null);

        }

    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
            write(tag);
    }


}
