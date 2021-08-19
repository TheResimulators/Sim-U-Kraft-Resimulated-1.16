package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.common.block.BlockControlBlock;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.init.ModBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class JobEditorStick extends Item {


    public JobEditorStick(Properties properties) {
        super(properties);
    }



    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack item = player.getItemInHand(hand);
        CompoundNBT nbt = item.getOrCreateTag();
        if(!world.isClientSide()) {
            if (player.isShiftKeyDown()){

                int id = nbt.getInt("tile entity");
                if (id >= BuildingType.getMaxValueId() || id < 2){
                    id = 2;
                }else{
                    id++;
                }
                player.displayClientMessage(new StringTextComponent(String.format("Switched tool to %s",BuildingType.getById(id).name)), true);
                nbt.putInt("tile entity",id);
            }

        }


        return super.use(world, player, hand);
    }


    @Override
    public ActionResultType useOn(ItemUseContext context) {
        System.out.println("level side " + !context.getLevel().isClientSide());
        if (!context.getLevel().isClientSide()) {
            if (context.getPlayer().isCrouching()){
                ItemStack item = context.getPlayer().getItemInHand(context.getHand());
                CompoundNBT nbt = item.getOrCreateTag();
                BlockPos pos = context.getClickedPos();
                BlockState state = context.getLevel().getBlockState(pos);
                if (state.getBlock() instanceof BlockControlBlock){
                TileEntity entity = context.getLevel().getBlockEntity(pos);
                TileEntityType<? extends TileEntity> target = BuildingType.getById(nbt.getInt("tile entity")).getType().get();
                if (entity.getType().getRegistryName() != target.getRegistryName() ){
                    state = state.setValue(ModBlockProperties.TYPE,nbt.getInt("tile entity"));
                    context.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(),11);
                    context.getLevel().setBlock(pos,state,11);
                    //((BlockControlBlock)context.getLevel().getBlockState(pos).getBlock()).createTileEntity(state,context.getLevel());
                    return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        return ActionResultType.FAIL;
    }
}
