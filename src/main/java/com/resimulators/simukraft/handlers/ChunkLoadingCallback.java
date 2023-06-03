package com.resimulators.simukraft.handlers;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;
import java.util.UUID;

public class ChunkLoadingCallback implements ForgeChunkManager.LoadingValidationCallback {



    @Override
    public void validateTickets(ServerWorld world, ForgeChunkManager.TicketHelper ticketHelper) {
        for (UUID entity: ticketHelper.getEntityTickets().keySet()) {
            ticketHelper.removeAllTickets(entity);
        }
    }
}
