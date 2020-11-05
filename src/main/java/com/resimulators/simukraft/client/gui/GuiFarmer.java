package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.packets.FarmerSeedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;

public class GuiFarmer extends GuiBaseJob {
    private Button seedButton;
    private Button confirmSeed;
    private TileFarmer farmer;
    private String title;
    public GuiFarmer(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.FARMER.getId());
        farmer = (TileFarmer) Minecraft.getInstance().world.getTileEntity(pos);
        title = component.getString();
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        addButton(seedButton = new Button(width/2-55, height-60, 110, 20, new StringTextComponent(StringUtils.capitalize(farmer.getSeed().getName())), (seedButton) -> {
            farmer.setSeed(Seed.getNextEnabledSeed(farmer.getSeed()));
            seedButton.setMessage(new StringTextComponent(StringUtils.capitalize(farmer.getSeed().getName())));
            confirmSeed.active = true;
        }));
        addButton(confirmSeed = new Button(width/2-55, height-30, 110, 20, new StringTextComponent("Confirm"), (seedButton) -> {
            Network.getNetwork().sendToServer(new FarmerSeedPacket(farmer.getSeed(),farmer.getPos()));
            confirmSeed.active = false;
        }));
        confirmSeed.active = false;
        mainMenu.add(confirmSeed);
        mainMenu.add(seedButton);
    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(stack,p_render_1_,p_render_2_,p_render_3_);
        if (state == State.MAIN){
            font.drawString(stack, "Select Seed", this.width/2-(font.getStringWidth("Select Seed")/2), this.height-80, Color.GREEN.getRGB());
            font.drawString(stack, "Level (Wip) " + StringUtils.capitalize(farmer.getSeed().getName()) + " Farm" , this.width/2-(font.getStringWidth("Level (Wip) " + farmer.getSeed().getName() + " Farm")/2), 20,new Color(76,153,0).brighter().getRGB() );
        }
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            if (state == State.MAIN) {
                TileFarmer tileEntity = (TileFarmer)world.getTileEntity(pos);
                if (tileEntity !=null) {
                    if (tileEntity.getMarker() != null) {
                        BlockPos marker = tileEntity.getMarker();
                        font.drawString(stack, "Markers Position:", 20, 90, Color.WHITE.getRGB());
                        font.drawString(stack, String.format("X: %d, Y: %d, Z: %d", marker.getX(), marker.getY(), marker.getZ()), 20, 110, Color.WHITE.getRGB());
                    }
                    if(tileEntity.getWidth() != 0){
                        font.drawString(stack, "Dimensions", width-50-font.getStringWidth("Dimensions")/2, 70, Color.YELLOW.getRGB());
                        font.drawString(stack, "Width: " + tileEntity.getWidth(), width-80, 90, Color.WHITE.getRGB());
                        font.drawString(stack, "Depth: " + tileEntity.getDepth(), width-80, 120, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }
}


