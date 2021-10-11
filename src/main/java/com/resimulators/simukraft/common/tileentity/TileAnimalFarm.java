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

public class TileAnimalFarm extends TileEntity implements IControlBlock {

    private final String name;
    int maxAnimals = 0;
    Animal entity;
    private boolean hired;
    private UUID simId;

    public TileAnimalFarm(TileEntityType<?> tileEntityTypeIn, Animal entity, String name) {
        super(tileEntityTypeIn);
        this.entity = entity;
        maxAnimals = 5;
        this.name = name;
        setChanged();

    }

    @Override
    public int getGui() {
        return GuiHandler.ANIMAL_FARM;
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
        return simId;
    }

    @Override
    public void setSimId(UUID id) {
        this.simId = id;
        setChanged();
    }

    @Override
    public void fireSim() {
        setHired(false);
        setSimId(null);
    }

    public String getName() {
        return name;
    }

    public void spawnAnimal() {
        if (level != null) {
            entity.getAnimal().spawn((ServerWorld) level, null, null, worldPosition.offset(0, 1, 0), SpawnReason.TRIGGERED, false, false);
        }

    }

    public boolean hasMaxAnimals() {
        return checkForAnimals() >= getMaxAnimals();
    }

    public int checkForAnimals() {
        if (level != null) {
            AxisAlignedBB area = new AxisAlignedBB(this.worldPosition.offset(-4, -1, -4), this.worldPosition.offset(4, 2, 4));
            List<AnimalEntity> entities = level.getEntitiesOfClass(AnimalEntity.class, area);
            entities = entities
                    .stream()
                    .filter(animal -> animal.getType() == entity.getAnimal())
                    .collect(Collectors.toList());
            return entities.size();

        }
        return maxAnimals; // basically force the tile entity not to spawn anything new
    }

    public int getMaxAnimals() {
        return maxAnimals;
    }

    public Animal getAnimal() {
        return entity;
    }


    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUUID("simid");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putBoolean("hired", this.hired);
        if (simId != null) {
            nbt.putUUID("simid", simId);
        }
        return nbt;
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound)
    {
        this.load(blockState, parentNBTTagCompound);
    }


}
