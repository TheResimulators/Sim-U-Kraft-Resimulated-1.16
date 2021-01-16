package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.enums.Animal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TileAnimalFarm extends TileEntity implements IControlBlock{

    private boolean hired;
    private UUID simId;
    int maxAnimals = 0;
    Animal entity;
    private final String name;

    public TileAnimalFarm(TileEntityType<?> tileEntityTypeIn, Animal entity, String name) {
        super(tileEntityTypeIn);
        this.entity = entity;
        maxAnimals = 5;
        this.name = name;
        markDirty();

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
        return simId;
    }

    @Override
    public void setSimId(UUID id) {
        this.simId = id;
        markDirty();
    }

    @Override
    public int getGui() {
        return GuiHandler.ANIMAL_FARM;
    }


    public void spawnAnimal(){
        if (world != null){
            entity.getAnimal().spawn((ServerWorld) world,null,null,pos.add(0,1,0), SpawnReason.TRIGGERED,false , false);
        }

    }

    public int checkForAnimals(){
        if (world != null){
        AxisAlignedBB area = new AxisAlignedBB(this.pos.add(-4,-1,-4),this.pos.add(4,2,4));
        List<AnimalEntity> entities = world.getEntitiesWithinAABB(AnimalEntity.class,area);
        entities = entities
                .stream()
                .filter(animal -> animal.getType() == entity.getAnimal())
                .collect(Collectors.toList());
        return entities.size();

        }
        return maxAnimals; // basically force the tile entity not to spawn anything new
    }

    public int getMaxAnimals(){
       return maxAnimals;
    }

    public boolean hasMaxAnimals(){
        return checkForAnimals() >= getMaxAnimals();
    }

    public String getName() {
        return name;
    }

    public Animal getAnimal(){
        return entity;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(this.getBlockState(),pkt.getNbtCompound());
    }
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }


    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putBoolean("hired", this.hired);
        if (simId != null) {
            nbt.putUniqueId("simid", simId);
        }
        return nbt;
    }



    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state,nbt);
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUniqueId("simid");
        }
    }
}
