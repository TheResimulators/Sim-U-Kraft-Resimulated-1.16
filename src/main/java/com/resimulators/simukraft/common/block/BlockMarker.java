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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockMarker extends BlockBase {
    public BlockMarker(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {

        ((TileMarker) worldIn.getBlockEntity(pos)).onRightClick(player.getMotionDirection());
        player.displayClientMessage(new StringTextComponent(String.format("Right Clicked Marker Scanned Markers Back Left: %s, Front Right: %s, Back Right: %s ", ((TileMarker) worldIn.getBlockEntity(pos)).getBackLeft(), ((TileMarker) worldIn.getBlockEntity(pos)).getFrontRight(), ((TileMarker) worldIn.getBlockEntity(pos)).getBackRight())), true);
        return ActionResultType.SUCCESS;
    }

    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return canSupportCenter(worldIn, pos.below(), Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return box(6, 0, 6, 10, 14, 10);
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        if (worldIn.getBlockEntity(pos) != null) {
            ((TileMarker) worldIn.getBlockEntity(pos)).onDestroy(pos);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileMarker();
    }
}
