package com.Resimulators.simukraft.common.events.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NewDayEvent implements INBTSerializable<CompoundNBT>{
    private double day = 0;
    private double previousDay = 0;

    @SubscribeEvent
    public void OnNewDayEvent(TickEvent.WorldTickEvent event){
        if (event.phase == TickEvent.Phase.END){
           double time =  event.world.getDayTime();
           day = Math.floor(time /24000);

           if (day != previousDay){
               payRent();
               previousDay = day;
               
           }
        }


    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble("day",day);
        nbt.putDouble("previousDay",previousDay);

        return nbt;
    }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            day = nbt.getDouble("day");
            previousDay = nbt.getDouble("previousDay");
        }


    public void payRent(){


    }
}
