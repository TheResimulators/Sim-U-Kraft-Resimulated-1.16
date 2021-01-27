package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.tileentity.IControlBlock;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.init.ModBlockProperties;
import com.resimulators.simukraft.packets.OpenJobGuiPacket;
import com.resimulators.simukraft.packets.SimFirePacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
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

public class BlockControlBox extends BlockBase {
    public static final IntegerProperty type = ModBlockProperties.TYPE;
    public BlockControlBox(Properties properties) {
        super(properties);
    }




    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote) {
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUniqueID());
            ArrayList<Integer> simids = faction.getSimIds((ServerWorld) world);
            IControlBlock controlBlock = (IControlBlock) world.getTileEntity(pos);
            if (controlBlock != null){
                if (controlBlock.getHired()){
                    Entity entity = ((ServerWorld) world).getEntityByUuid(controlBlock.getSimId());
                    if (entity != null){
                        int hiredId = entity.getEntityId();
                        SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,hiredId, controlBlock.getGui(), controlBlock.getName()),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);// used when there is a sim hired
                    }
                } else {
                    SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids,pos,controlBlock.getGui(),controlBlock.getName()),((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);//used when there is no sim employed at this block
                }
            }

        }
        return ActionResultType.SUCCESS;
    }


    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }


    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (state.hasProperty(ModBlockProperties.TYPE)){
            int typeId = state.get(ModBlockProperties.TYPE);
            BuildingType type = BuildingType.getById(typeId);
            if (type != null){
                TileEntity entity = type.type.get().create();
                return entity;

            }
        }
        return new TileCustomData();
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (!worldIn.isRemote){

            ITile tile = ((ITile) worldIn.getTileEntity(pos));

            SimEntity sim = (SimEntity) ((ServerWorld)worldIn).getEntityByUuid(tile.getSimId());
            if (sim != null){
                int id = SavedWorldData.get(worldIn).getFactionWithPlayer(player.getUniqueID()).getId();
                SavedWorldData.get(worldIn).fireSim(id,sim);
                SavedWorldData.get(worldIn).getFaction(id).sendPacketToFaction(new SimFirePacket(id,sim.getEntityId(),pos));
                sim.fireSim(sim,id,false);
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(type);
        super.fillStateContainer(builder);
    }
}
