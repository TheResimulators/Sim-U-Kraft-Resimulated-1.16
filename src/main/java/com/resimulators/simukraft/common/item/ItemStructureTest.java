package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.common.item.interfaces.IStructureStorage;
import com.resimulators.simukraft.handlers.StructureHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStructureTest extends Item implements IStructureStorage {
    private BlockPos placementArea;

    public ItemStructureTest(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getPlayer() != null) {
            if (context.getPlayer().isCrouching()) {
                placementArea = context.getPos();
                context.getPlayer().sendStatusMessage(new StringTextComponent("Set Placement Area: " + placementArea), false);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (playerIn.isCrouching()) {
            placementArea = null;
        }

        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent(((ItemStructureTest)stack.getItem()).getStructure(stack) + "   " + placementArea));
    }

    public BlockPos getPlacementArea() {
        return placementArea;
    }

    public Template getTemplate(String name) {
        return StructureHandler.loadStructure(name);
    }

    public void setTemplate(ItemStack stack, String name) {
        setStructure(stack, name);
    }
}
