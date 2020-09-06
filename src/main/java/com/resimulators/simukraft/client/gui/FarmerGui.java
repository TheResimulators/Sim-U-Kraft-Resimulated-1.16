package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.enums.Seed;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.tileentity.TileFarmer;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import com.resimulators.simukraft.packets.FarmerSeedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.codehaus.plexus.util.StringUtils;

import java.awt.*;
import java.util.ArrayList;

public class FarmerGui extends BaseJobGui {
    private Button seedButton;
    private Button confirmSeed;
    private TileFarmer farmer;
    private String title;
    public FarmerGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.FARMER.getId());
        farmer = (TileFarmer) Minecraft.getInstance().world.getTileEntity(pos);
        title = component.getString();
    }


    @Override
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);
        func_230480_a_(seedButton = new Button(width/2-55, height-60, 110, 20, new StringTextComponent(StringUtils.capitalizeFirstLetter(farmer.getSeed().getName())), (seedButton) -> {
            farmer.setSeed(Seed.getNextEnabledSeed(farmer.getSeed()));
            seedButton.func_238482_a_(new StringTextComponent(StringUtils.capitalizeFirstLetter(farmer.getSeed().getName())));
            confirmSeed.field_230693_o_ = true;
        }));
        func_230480_a_(confirmSeed = new Button(width/2-55, height-30, 110, 20, new StringTextComponent("Confirm"), (seedButton) -> {
            Network.getNetwork().sendToServer(new FarmerSeedPacket(farmer.getSeed(),farmer.getPos()));
            confirmSeed.field_230693_o_ = false;
        }));
        confirmSeed.field_230693_o_ = false;
        mainMenu.add(confirmSeed);
        mainMenu.add(seedButton);
    }

    @Override
    public void func_230430_a_(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.func_230430_a_(stack,p_render_1_,p_render_2_,p_render_3_);
        if (state == State.MAIN){
            field_230712_o_.func_238421_b_(stack, "Select Seed", this.field_230708_k_/2-(field_230712_o_.getStringWidth("Select Seed")/2), this.field_230709_l_-80, Color.GREEN.getRGB());
            field_230712_o_.func_238421_b_(stack, "Level (Wip) " + StringUtils.capitalizeFirstLetter(farmer.getSeed().getName()) + " Farm" , this.field_230708_k_/2-(field_230712_o_.getStringWidth("Level (Wip) " + farmer.getSeed().getName() + " Farm")/2), 20,new Color(76,153,0).brighter().getRGB() );
        }
        World world = SimuKraft.proxy.getClientWorld();
        if (world != null) {
            if (state == State.MAIN) {
                TileFarmer tileEntity = (TileFarmer)world.getTileEntity(pos);
                if (tileEntity !=null) {
                    if (tileEntity.getMarker() != null) {
                        BlockPos marker = tileEntity.getMarker();
                        field_230712_o_.func_238421_b_(stack, "Markers Position:", 20, 90, Color.WHITE.getRGB());
                        field_230712_o_.func_238421_b_(stack, String.format("X: %d, Y: %d, Z: %d", marker.getX(), marker.getY(), marker.getZ()), 20, 110, Color.WHITE.getRGB());
                    }
                    if(tileEntity.getWidth() != 0){
                        field_230712_o_.func_238421_b_(stack, "Dimensions", field_230708_k_-50-field_230712_o_.getStringWidth("Dimensions")/2, 70, Color.YELLOW.getRGB());
                        field_230712_o_.func_238421_b_(stack, "Width: " + tileEntity.getWidth(), field_230708_k_-80, 90, Color.WHITE.getRGB());
                        field_230712_o_.func_238421_b_(stack, "Depth: " + tileEntity.getDepth(), field_230708_k_-80, 120, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }
}


