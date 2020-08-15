package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.SimFirePacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockBase extends Block {
    public BlockBase(Properties properties) {
        super(properties);
    }


    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (!worldIn.isRemote){

            ITile tile = ((ITile) worldIn.getTileEntity(pos));

            SimEntity sim =(SimEntity) ((ServerWorld)worldIn).getEntityByUuid(tile.getSimId());
            if (sim != null){
                int id = SavedWorldData.get(worldIn).getFactionWithPlayer(player.getUniqueID()).getId();
                SavedWorldData.get(worldIn).fireSim(id,sim);
                SavedWorldData.get(worldIn).getFaction(id).sendPacketToFaction(new SimFirePacket(id,sim.getEntityId(),pos));
                sim.getJob().removeJobAi();
                sim.setJob(null);
                sim.setProfession(0);
                tile.setHired(false);
                tile.setSimId(null);}
        }
    }
}
