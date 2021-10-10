package com.resimulators.simukraft.common.events.world;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class MarkerBrokenEvent {


    @SubscribeEvent
    public static void onMarkerDestroyed(BlockEvent.BreakEvent event) {
        if (event.getWorld().getBlockEntity(event.getPos()) instanceof TileMarker) {
            TileMarker marker = (TileMarker) event.getWorld().getBlockEntity(event.getPos());
            marker.onDestroy(event.getPos());
        }
    }
}
