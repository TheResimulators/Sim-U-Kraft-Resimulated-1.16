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

    @Override
    public void read(BlockState state, CompoundNBT compound) {

        dir = Direction.byIndex(compound.getInt("dir"));
        if (compound.contains("sim id")) {
            simID = UUID.fromString(compound.getString("sim id"));
        }
        hired = compound.getBoolean("hired");
        if (compound.contains("width")){
            width = compound.getInt("width");
        }
        if (compound.contains("depth")){
            depth = compound.getInt("depth");
        }
        if (compound.contains("height")){
            height = compound.getInt("height");
        }
        if (compound.contains("marker")){
            marker = BlockPos.fromLong(compound.getLong("marker"));
        }
        super.read(state, compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (dir != null) {
            compound.putInt("dir", dir.getIndex());
        }
        if (simID != null) {
            compound.putString("sim id", simID.toString());
        }
        compound.putBoolean("hired", hired);
        if (width != 0){
            compound.putInt("width",width);
        }
        if (depth != 0){
            compound.putInt("depth",depth);
        }
        if (height != 0){
            compound.putInt("height",height);
        }
        if (marker != null){
            compound.putLong("marker",marker.toLong());
        }
        return super.write(compound);
    }


    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        markDirty();
    }

    @Override
    public boolean getHired() {
        return hired;
    }

    @Override
    public UUID getSimId() {
        return simID;

    }

    @Override
    public void setSimId(UUID id) {
        this.simID = id;
        markDirty();
    }


    public void setMarker(BlockPos pos) {
        marker = pos;
    }


    public void onOpenGui(Direction dir) {
        this.dir = dir;
        markDirty();
        Scan();


    }

    public BlockPos getMarker() {
        return marker;
    }

    public void Scan() {
        if (dir != null) {
            if (world != null){
                if (world.getTileEntity(pos.offset(dir)) instanceof TileMarker) {
                    setMarker(pos.offset(dir));
                    setDimensions();
                    markDirty();
                    world.notifyBlockUpdate(getPos(),getBlockState(),getBlockState(),2);
                }
            }
        }
    }

    private void setDimensions() {
        if (marker == null){Scan();}
        width = marker.manhattanDistance(((TileMarker) this.world.getTileEntity(marker)).getFrontRight());
        depth = marker.manhattanDistance(((TileMarker) this.world.getTileEntity(marker)).getBackLeft());
        height = marker.getY() - 1;
        markDirty();

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
        return dir;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    public boolean CheckValidity() {
        if (this.world != null) {
            TileEntity mark = this.world.getTileEntity(marker);
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
        read(this.getBlockState(),pkt.getNbtCompound());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
    }
}