package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class MinerGui extends BaseJobGui {
    Button scan;
    public MinerGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, @Nullable int id) {
        super(component, ids, pos, id);
        this.job = Profession.MINER.getName();

    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        addButton(scan = new Button(width-100,30,80,20,"Scan",Scan->{
            if (((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).getDir() == null){
                ((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).onOpenGui(Minecraft.getInstance().player.getAdjustedHorizontalFacing());
            }
            ((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).Scan();


        }));
        if (!((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).getHired()){
            scan.active = false;
        }

        }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(p_render_1_,p_render_2_,p_render_3_);
        if (((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).getMarker() != null){
            if (state == State.MAIN){
                BlockPos marker = ((TileMiner)Minecraft.getInstance().world.getTileEntity(pos)).getMarker();
                font.drawString("Markers Position:",20,90, Color.WHITE.getRGB());
                font.drawString(String.format("X: %d, Y: %d, Z: %d",marker.getX(),marker.getY(),marker.getZ()),20,110,Color.WHITE.getRGB());
            }
        }
    }

    @Override
    public void showMainMenu(){
        super.showMainMenu();
    }



}
