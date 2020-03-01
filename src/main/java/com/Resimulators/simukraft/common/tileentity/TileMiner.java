package com.Resimulators.simukraft.common.tileentity;

import com.Resimulators.simukraft.init.ModTileEntities;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.UUID;

public class TileMiner extends TileEntity implements ITile {
    private boolean hired;
    private UUID simID;
    private Direction dir;

    private BlockPos marker;

    @Override
    public void read(CompoundNBT compound) {
        dir = Direction.byIndex(compound.getInt("dir"));
        if (compound.contains("sim id")){
            simID = compound.getUniqueId("sim id");
        }
        hired = compound.getBoolean("hired");
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("dir",dir.getIndex());
        if (simID != null){
            compound.putUniqueId("sim id",simID);
        }
        compound.putBoolean("hired",hired);
        return super.write(compound);
    }

    public TileMiner() {
        super(ModTileEntities.MINER);
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


    public void setMarker(BlockPos pos)
    {
        marker = pos;
    }


    public void onOpenGui(Direction dir){
        this.dir = dir;
        markDirty();
        Scan();




    }

    public BlockPos getMarker(){
        return marker;
    }

    public void Scan(){
        if (dir != null ){
        if (world.getTileEntity(pos.offset(dir)) instanceof TileMarker){
            setMarker(pos.offset(dir));
            markDirty();
            }
        }
    }
    // gets the distance to the back left. because it shoul be same y and
    // another axis this should be a single number which is the distance between them
    public int getWidth(){
        return marker.manhattanDistance(((TileMarker)this.world.getTileEntity(marker)).getFrontRight());
    }

    public int getDepth(){
        return marker.manhattanDistance(((TileMarker)this.world.getTileEntity(marker)).getBackLeft());
    }

    public int getYpos(){
        return marker.getY()-1; // most likely not needed just putting it here incase i do actually need it
    }


    public Direction getDir(){
        return dir;
    }
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT nbt) {
        read(nbt);
    }



}
