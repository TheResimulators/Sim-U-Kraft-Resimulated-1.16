package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;


public class TileCustomData extends TileEntity implements IControlBlock {
    private BuildingType type;
    private float price;
    private float rent;
    private int width = 0;
    private int height = 0;
    private int depth = 0;

    public TileCustomData() {
        super(ModTileEntities.CUSTOM_DATA.get());
        type = BuildingType.RESIDENTIAL;

    }

    @Override
    public int getGui() {
        return GuiHandler.CUSTOM_DATA;
    }

    @Override
    public boolean getHired() {
        return false;
    }

    @Override
    public void setHired(boolean hired) {

    }

    @Override
    public UUID getSimId() {
        return null;
    }

    @Override
    public void setSimId(UUID id) {

    }

    @Override
    public String getName() {
        return "Control Block";
    }

    public BuildingType getBuildingType() {
        return type;
    }

    public void setBuildingType(BuildingType type) {
        this.type = type;
        setChanged();
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
        setChanged();
    }

    public float getRent() {
        return rent;
    }

    public void setRent(float rent) {
        this.rent = rent;
        setChanged();
    }

    public void calculatePrice() {
        //TODO: automatically calculate the price based on size and materials
        //fabbe50: you are probably best to do this
    }

    public void calculateRent() {
        //TODO: automatically calculate rent based on size. this is only used in residential buildings
        //fabbe50: this as well
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        setChanged();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        setChanged();
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        setChanged();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compoundNBT) {
        if (compoundNBT.contains("building type")) {
            setBuildingType(BuildingType.getById(compoundNBT.getInt("building type")));
            setPrice(compoundNBT.getFloat("price"));
            setRent(compoundNBT.getFloat("rent"));
        }
        super.load(blockState, compoundNBT);
    }


    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putInt("building type", type.id);
        compound.putFloat("price", this.price);
        compound.putFloat("rent", this.rent);

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
    public void fireSim() {
        setHired(false);
        setSimId(null);
    }
}