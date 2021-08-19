package com.resimulators.simukraft.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class JobEditorStick extends Item {


    public JobEditorStick(Properties properties) {
        super(properties);
    }



    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        if(!player.level.isClientSide()) {
            if (player.isShiftKeyDown()){


            }

        }


        return super.use(world, player, hand);
    }

}
