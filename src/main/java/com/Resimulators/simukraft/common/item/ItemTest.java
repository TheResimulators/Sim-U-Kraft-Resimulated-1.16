package com.Resimulators.simukraft.common.item;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;

public class ItemTest extends Item {
    public ItemTest(Properties properties) {
        super(properties);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof EntitySim) {
            player.sendStatusMessage(new StringTextComponent("" + (((EntitySim) entity).getSelectedSlot())), true);
            ((EntitySim) entity).selectSlot(((EntitySim) entity).getSelectedSlot() + 1);
        }
        return true;
    }
}
