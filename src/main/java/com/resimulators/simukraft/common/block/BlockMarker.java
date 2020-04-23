package com.resimulators.simukraft.common.block;

import com.resimulators.simukraft.common.tileentity.TileMarker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITargetedTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockMarker extends BlockBase {
    public BlockMarker(Properties properties, String name) {
        super(properties, name);
    }


    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        return makeCuboidShape(6,0,6,10,14,10);
    }
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (worldIn.getTileEntity(pos) != null){
        ((TileMarker)worldIn.getTileEntity(pos)).onDestroy(pos);}
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {

        ((TileMarker)worldIn.getTileEntity(pos)).onRightClick(player.getAdjustedHorizontalFacing());
        player.sendStatusMessage(new StringTextComponent(String.format("Right Clicked Marker Scanned Markers Back Left: %s, Front Right: %s, Back Right: %s ", ((TileMarker)worldIn.getTileEntity(pos)).getBackLeft(), ((TileMarker)worldIn.getTileEntity(pos)).getFrontRight(), ((TileMarker)worldIn.getTileEntity(pos)).getBackRight() )),true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileMarker();
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
