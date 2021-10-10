package com.resimulators.simukraft.common.enums;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;

public enum Animal {


    COW("cow", EntityType.COW, 1),
    SHEEP("sheep", EntityType.SHEEP, 2),
    PIG("pig", EntityType.PIG, 3),
    CHICKEN("Chicken", EntityType.CHICKEN, 4);

    String name;
    EntityType<? extends AnimalEntity> animal;
    int id;

    Animal(String name, EntityType<? extends AnimalEntity> animal, int id) {
        this.name = name;
        this.animal = animal;
        this.id = id;
    }


    public EntityType<? extends AnimalEntity> getAnimal() {
        return animal;
    }
}
