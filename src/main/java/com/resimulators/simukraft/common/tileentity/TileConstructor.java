package com.resimulators.simukraft.common.tileentity;

import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.building.CustomTemplateManager;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.init.ModTileEntities;
import com.resimulators.simukraft.packets.BuildingsPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.UUID;

public class TileConstructor extends TileEntity implements ITile {

    private boolean hired;
    private UUID simId;
    private BlockPos cornerPosition;
    private BlockPos origin;

    public TileConstructor() {
        super(ModTileEntities.CONSTRUCTOR.get());
    }

    @Override
    public boolean getHired() {
        return hired;
    }

    @Override
    public void setHired(boolean hired) {
        this.hired = hired;
        setChanged();
    }

    @Override
    public UUID getSimId() {
        return simId;
    }

    @Override
    public void setSimId(UUID id) {
        this.simId = id;
        setChanged();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        hired = nbt.getBoolean("hired");
        if (nbt.contains("simid")) {
            simId = nbt.getUUID("simid");
        }
        if (nbt.contains("origin")){
            origin = BlockPos.of(nbt.getLong("origin"));
            cornerPosition = BlockPos.of(nbt.getLong("cornerPos"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putBoolean("hired", hired);
        if (simId != null) {
            nbt.putUUID("simid", simId);
        }
        if (this.origin != null){
            nbt.putLong("origin",origin.asLong());
            nbt.putLong("cornerPos",cornerPosition.asLong());
        }
        return nbt;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    public void FindAndLoadBuilding(PlayerEntity playerEntity) {
        if (CustomTemplateManager.isInitialized()) {
            ArrayList<BuildingTemplate> templates = CustomTemplateManager.getAllBuildingTemplates();
            Network.getNetwork().sendToPlayer(new BuildingsPacket(templates), (ServerPlayerEntity) playerEntity);
        }
    }


    public void setBuildingPositioning(BlockPos size, Direction direction){

        cornerPosition = size;
                //BlockPos.ZERO.relative(direction).relative(direction.getClockWise(),size.getX()).relative(direction,size.getZ()).above(size.getY());

        this.origin = this.getBlockPos().relative(direction);
        this.level.setBlock(origin.offset(0,3,0), Blocks.COBBLESTONE.defaultBlockState(),3);
        this.level.setBlock(cornerPosition.offset(0,3,0), Blocks.COBBLESTONE.defaultBlockState(),3);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        setChanged();

    }
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        return new SUpdateTileEntityPacket(this.worldPosition,-1,this.getUpdateTag());
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public BlockPos getCornerPosition() {
        return cornerPosition;
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT parentNBTTagCompound)
    {
        this.load(blockState, parentNBTTagCompound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getBlockPos(),cornerPosition);
    }
}
