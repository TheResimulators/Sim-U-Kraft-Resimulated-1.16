package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.core.Activity;
import com.resimulators.simukraft.common.jobs.reworked.JobBuilder;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import com.resimulators.simukraft.handlers.StructureHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class StartBuildingPacket implements IMessage {

    private BlockPos pos;
    private Direction direction;
    private String name;
    private UUID playerId;

    public StartBuildingPacket() {
    }

    public StartBuildingPacket(BlockPos pos, Direction dir, String name, UUID playerId) {
        this.pos = pos;
        direction = dir;
        this.name = name;
        this.playerId = playerId;

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(direction.get2DDataValue());
        buf.writeUtf(name);
        buf.writeUUID(playerId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        pos = buf.readBlockPos();
        direction = Direction.from2DDataValue(buf.readInt());
        name = buf.readUtf(32767);
        playerId = buf.readUUID();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId) != null) {
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                TileConstructor constructor = (TileConstructor) player.getCommandSenderWorld().getBlockEntity(pos);
                SimEntity sim = (SimEntity) ((ServerWorld) player.getCommandSenderWorld()).getEntity(constructor.getSimId());
                JobBuilder builder = (JobBuilder) sim.getJob();
                builder.setDirection(direction);
                BuildingTemplate template = StructureHandler.loadStructure(name);
                builder.setTemplate(template);
                sim.setActivity(Activity.GOING_TO_WORK);
                constructor.setBuilding(true);
                constructor.setWageTotal(template.getCost());
                constructor.setWagePerBlock(template.getCost()/ template.totalItemsNeeded());

            }
        }


    }
}
