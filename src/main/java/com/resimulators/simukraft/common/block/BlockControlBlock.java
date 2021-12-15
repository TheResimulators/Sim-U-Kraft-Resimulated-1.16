package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.tileentity.IControlBlock;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import com.resimulators.simukraft.common.tileentity.TileResidential;
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

public class BlockControlBlock extends BlockBase {
    public static final IntegerProperty type = ModBlockProperties.TYPE;

    public BlockControlBlock(Properties properties) {
        super(properties);
    }


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isClientSide) {
            Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUUID());
            ArrayList<Integer> simids = faction.getSimIds((ServerWorld) world);
            IControlBlock controlBlock = (IControlBlock) world.getBlockEntity(pos);

            if (controlBlock != null) {
                if (controlBlock.getHired()) {
                    Entity entity = ((ServerWorld) world).getEntity(controlBlock.getSimId());
                    if (entity != null) {
                        int hiredId = entity.getId();
                        SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids, pos, hiredId, controlBlock.getGui(), controlBlock.getName()), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);// used when there is a sim hired
                    }
                } else {
                    SimUKraftPacketHandler.INSTANCE.sendTo(new OpenJobGuiPacket(simids, pos, controlBlock.getGui(), controlBlock.getName()), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);//used when there is no sim employed at this block
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
        if (state.hasProperty(ModBlockProperties.TYPE)) {
            int typeId = state.getValue(ModBlockProperties.TYPE);
            BuildingType type = BuildingType.getById(typeId);
            return type.getType().get().create();

        }
        return new TileCustomData();
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        if (!worldIn.isClientSide) {

            ITile tile = ((ITile) worldIn.getBlockEntity(pos));
            if (tile != null) {
                if (!(tile instanceof TileResidential)) {
                    SimEntity sim = (SimEntity) ((ServerWorld) worldIn).getEntity(tile.getSimId());
                    if (sim != null) {
                        int id = SavedWorldData.get(worldIn).getFactionWithPlayer(player.getUUID()).getId();
                        SavedWorldData.get(worldIn).fireSim(id, sim);
                        SavedWorldData.get(worldIn).getFaction(id).sendPacketToFaction(new SimFirePacket(id, sim.getId(), pos, false));
                        sim.fireSim(sim, id, false);
                    }
                } else {
                    ((TileResidential) tile).onDestroy(worldIn);
                }
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(type);
        super.createBlockStateDefinition(builder);
    }
}
