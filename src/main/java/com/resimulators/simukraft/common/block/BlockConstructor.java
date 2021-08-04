package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.packets.OpenJobGuiPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;

public class BlockConstructor extends BlockBase {
    public BlockConstructor(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileConstructor();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isClientSide) {
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUUID());
            ArrayList<Integer> simids = faction.getSimIds((ServerWorld) world);
            System.out.println(world.getBlockEntity(pos));

            if (((ITile) world.getBlockEntity(pos)).getHired()) {
                int hiredId = ((ServerWorld) world).getEntity(((ITile) world.getBlockEntity(pos)).getSimId()).getId();
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids, pos, hiredId, GuiHandler.BUILDER, "Constructor"), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);// used when there is a sim hired
            } else {
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids, pos, GuiHandler.BUILDER, "Constructor"), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);//used when there is no sim employed at this block
            }
            if (world.getBlockEntity(pos) instanceof TileConstructor) {
                ((TileConstructor) world.getBlockEntity(pos)).FindAndLoadBuilding(player);
            }

        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        if (!worldIn.isClientSide) {

            ITile tile = ((ITile) worldIn.getBlockEntity(pos));

            SimEntity sim = (SimEntity) ((ServerWorld) worldIn).getEntity(tile.getSimId());
            if (sim != null) {
                int id = SavedWorldData.get(worldIn).getFactionWithSim(sim.getUUID()).getId();
                sim.fireSim(sim, id, false);
            }
        }

    }
}
