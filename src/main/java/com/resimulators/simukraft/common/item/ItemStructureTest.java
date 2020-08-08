package com.resimulators.simukraft.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

public class ItemStructureTest extends Item {
    private BlockPos pos1;
    private BlockPos pos2;
    private BlockPos placementArea;
    private Template template;

    public ItemStructureTest(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getPlayer() != null) {
            if (pos1 == null) {
                pos1 = context.getPos();
                context.getPlayer().sendStatusMessage(new StringTextComponent("Set Pos1 to: " + pos1), false);
            } else if (pos2 == null) {
                pos2 = context.getPos();
                context.getPlayer().sendStatusMessage(new StringTextComponent("Set Pos2 to: " + pos2), false);
            }
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
            pos1 = null;
            pos2 = null;
            placementArea = null;
        }

        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public BlockPos getPlacementArea() {
        return placementArea;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Template getTemplate() {
        return template;
    }
}
