package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;

public class ItemTest extends Item {
    public ItemTest(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof SimEntity) {
            player.displayClientMessage(new StringTextComponent("" + (((SimEntity) entity).getSelectedSlot())), true);
            ((SimEntity) entity).selectSlot(((SimEntity) entity).getSelectedSlot() + 1);
        }
        return ActionResultType.SUCCESS;
    }
}
