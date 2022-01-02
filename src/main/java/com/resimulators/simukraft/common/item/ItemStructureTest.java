package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.item.interfaces.IStructureStorage;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.reworked.JobBuilder;
import com.resimulators.simukraft.handlers.StructureHandler;
import com.resimulators.simukraft.init.ModJobs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStructureTest extends Item implements IStructureStorage {
    private BlockPos placementArea;
    private Direction direction;

    public ItemStructureTest(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getPlayer() != null) {
            //sets placement position for building
            if (context.getPlayer().isCrouching()) {
                placementArea = context.getClickedPos();
                direction = context.getHorizontalDirection();
                context.getPlayer().displayClientMessage(new StringTextComponent("Set Placement Area: " + placementArea), false);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (playerIn.isCrouching()) { // if crouching reset placement area so a new area can be set
            placementArea = null;
        }

        return ActionResult.success(playerIn.getItemInHand(handIn));
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        BuildingTemplate temp = this.getTemplate(this.getStructure(stack));
        System.out.println(temp != null);
        //sets sims job to builder and relevant info needed for it to work
        if (temp != null && target instanceof SimEntity) {
            ((SimEntity) target).setProfession(Profession.BUILDER.getId());
            ((SimEntity) target).setJob(ModJobs.JOB_LOOKUP.get(Profession.BUILDER.getId()).apply((SimEntity) target));
            if (((SimEntity) target).getJob() instanceof JobBuilder) {
                ((JobBuilder) ((SimEntity) target).getJob()).setTemplate(temp);
                ((JobBuilder) ((SimEntity) target).getJob()).setDirection(direction);
                ((SimEntity) target).getJob().setWorkSpace(placementArea.relative(direction.getOpposite()));
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        //adds info to tooltip when structure is loaded
        tooltip.add(new StringTextComponent(((ItemStructureTest) stack.getItem()).getStructure(stack) + "   " + placementArea));
    }

    /** gets template that this item is linked to */
    public BuildingTemplate getTemplate(String name) {
        return StructureHandler.loadStructure(name);
    }

    public BlockPos getPlacementArea() {
        return placementArea;
    }

    /** sets template this item is connected to */
    public void setTemplate(ItemStack stack, String name) {
        setStructure(stack, name);
    }

    public Direction getDirection() {
        return direction;
    }
}
