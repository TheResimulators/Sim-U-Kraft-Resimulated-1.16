package com.Resimulators.simukraft.common.events.world;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.tileentity.TileMarker;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class MarkerBrokenEvent {


    @SubscribeEvent
    public static void onMarkerDestroyed(BlockEvent.BreakEvent event){
        if (event.getWorld().getTileEntity(event.getPos()) instanceof TileMarker){
       TileMarker marker = (TileMarker) event.getWorld().getTileEntity(event.getPos());
       marker.onDestroy(event.getPos());
        }
    }
}
