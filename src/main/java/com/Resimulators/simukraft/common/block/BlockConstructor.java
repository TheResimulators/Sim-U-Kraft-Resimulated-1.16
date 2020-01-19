package com.Resimulators.simukraft.common.block;

import com.Resimulators.simukraft.client.interfaces.BaseJobGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockConstructor extends BlockBase {
    public BlockConstructor(final Properties properties,String name) {
        super(properties,name);

    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (world.isRemote){
            Minecraft.getInstance().displayGuiScreen(new BaseJobGui(new StringTextComponent("hello")));
        }
        return ActionResultType.SUCCESS;


    }
}
