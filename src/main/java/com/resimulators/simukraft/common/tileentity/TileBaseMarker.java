package com.resimulators.simukraft.common.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class TileBaseMarker extends TileEntity implements ITile {
    private boolean hired;
    private UUID simID;
    private Direction dir;

    private BlockPos marker;
    private int width;
    private int depth;
    private int height;

    public TileBaseMarker(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public void onOpenGui(Direction dir) {
        this.dir = dir;
        setChanged();
        Scan();
    }

    public BlockPos getMarker() {
        return marker;
    }

    public void setMarker(BlockPos pos) {
        marker = pos;
    }

    public void Scan() {
        if (dir != null) {
            if (level != null) {
                if (level.getBlockEntity(worldPosition.relative(dir)) instanceof TileMarker) {
                    setMarker(worldPosition.relative(dir));
                    setDimensions();
                    setChanged();
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
                }
            }
        }
    }

    private void setDimensions() {
        if (marker == null) {
            Scan();
        }
        width = marker.distManhattan(((TileMarker) this.level.getBlockEntity(marker)).getFrontRight());
        depth = marker.distManhattan(((TileMarker) this.level.getBlockEntity(marker)).getBackLeft());
        height = marker.getY() - 1;
        setChanged();

    }

    // gets the distance to the back left. because it shoul be same y and
    // another axis this should be a single number which is the distance between them
    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int getYpos() {
        return height; // most likely not needed just putting it here in case i do actually need it
    }

    public Direction getDir() {
        TileEntity mark = this.level.getBlockEntity(marker);
        if (mark instanceof TileMarker) {
            return ((TileMarker) mark).getOrientation();
        }
        return null;
        //return dir;
    }

    public boolean CheckValidity() {
        if (this.level != null) {
            TileEntity mark = this.level.getBlockEntity(marker);
            if (mark instanceof TileMarker) {
                BlockPos frontRight = ((TileMarker) mark).getFrontRight();
                BlockPos backLeft = ((TileMarker) mark).getBackLeft();
                return frontRight != null && backLeft != null;
            }
        }
        return false;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        dir = Direction.from3DDataValue(compound.getInt("dir"));
        if (compound.contains("sim id")) {
            simID = UUID.fromString(compound.getString("sim id"));
        }
        hired = compound.getBoolean("hired");
        if (compound.contains("width")) {
            width = compound.getInt("width");
        }
        if (compound.contains("depth")) {
            depth = compound.getInt("depth");
        }
        if (compound.contains("height")) {
            height = compound.getInt("height");
        }
        if (compound.contains("marker")) {
            marker = BlockPos.of(compound.getLong("marker"));
        }
        super.load(state, compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if (dir != null) {
            compound.putInt("dir", dir.get3DDataValue());
        }
        if (simID != null) {
            compound.putString("sim id", simID.toString());
        }
        compound.putBoolean("hired", hired);
        if (width != 0) {
            compound.putInt("width", width);
        }
        if (depth != 0) {
            compound.putInt("depth", depth);
        }
        if (height != 0) {
            compound.putInt("height", height);
        }
        if (marker != null) {
            compound.putLong("marker", marker.asLong());
        }
        return super.save(compound);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound) {
        this.load(blockState, parentNBTTagCompound);
    }

    @Override
    public boolean getHired() {
        return hired;
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        setChanged();
    }

    @Override
    public UUID getSimId() {
        return simID;

    }


    @Override
    public void setSimId(UUID id) {
        this.simID = id;
        setChanged();
    }


    @Override
    public void fireSim() {
        setHired(false);
        setSimId(null);
    }


}