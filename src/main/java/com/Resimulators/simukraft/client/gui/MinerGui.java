package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.common.jobs.Profession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class MinerGui extends BaseJobGui {
    Button scan;

    public MinerGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.MINER.getId());
    }

    @Override
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);
        func_230480_a_(scan = new Button(width - 100, 30, 80, 20, new StringTextComponent("Scan"), Scan -> {
            if (((TileMiner) Minecraft.getInstance().world.getTileEntity(pos)).getDir() == null) {
                ((TileMiner) Minecraft.getInstance().world.getTileEntity(pos)).onOpenGui(Minecraft.getInstance().player.getAdjustedHorizontalFacing());
            }
            ((TileMiner) Minecraft.getInstance().world.getTileEntity(pos)).Scan();
        }));
        if (!((TileMiner) Minecraft.getInstance().world.getTileEntity(pos)).getHired()) {
            scan.field_230693_o_ = false;
        }

    }

    @Override
    public void func_230430_a_(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.func_230430_a_(stack, p_render_1_, p_render_2_, p_render_3_);
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileMiner) {
                if (((TileMiner)tileEntity).getMarker() != null) {
                    BlockPos marker = ((TileMiner)tileEntity).getMarker();
                    if (state == State.MAIN) {
                        field_230712_o_.func_238421_b_(stack, "Markers Position:", 20, 90, Color.WHITE.getRGB());
                        field_230712_o_.func_238421_b_(stack, String.format("X: %d, Y: %d, Z: %d", marker.getX(), marker.getY(), marker.getZ()), 20, 110, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
    }
}
