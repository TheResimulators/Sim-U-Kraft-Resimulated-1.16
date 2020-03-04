package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.gui.GuiHandler;
import com.resimulators.simukraft.common.entity.sim.EntitySim;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.packets.OpenJobGuiPacket;
import com.resimulators.simukraft.packets.SimFirePacket;
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
    public BlockConstructor(final Properties properties, String name) {
        super(properties, name);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote) {
            SimuKraft.LOGGER().debug("Tile Entity At Pos = " + world.getTileEntity(pos));
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUniqueID());
            ArrayList<Integer> simids = faction.getSimUnemployedIds((ServerWorld) world);
            System.out.println(world.getTileEntity(pos));
            if (((ITile)world.getTileEntity(pos)).getHired()){
                int hiredId = ((ServerWorld) world).getEntityByUuid(((ITile)world.getTileEntity(pos)).getSimId()).getEntityId();
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,hiredId, GuiHandler.Builder),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                } else {
                SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,GuiHandler.Builder),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileConstructor();
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (!worldIn.isRemote){

            ITile tile = ((ITile) worldIn.getTileEntity(pos));

            EntitySim sim =(EntitySim) ((ServerWorld)worldIn).getEntityByUuid(tile.getSimId());
            if (sim != null){
            int id = SavedWorldData.get(worldIn).getFactionWithPlayer(player.getUniqueID()).getId();
            SavedWorldData.get(worldIn).fireSim(id,sim);
            SavedWorldData.get(worldIn).getFaction(id).sendPacketToFaction(new SimFirePacket(id,sim.getEntityId(),pos));
            tile.setHired(false);
            tile.setSimId(null);}
        }
    }


}
