package com.Resimulators.simukraft.client.gui;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class BaseJobGui extends Screen {
    private Button Hire;
    private Button Fire;
    private Button ShowEmployees;
    private Button Done;
    
    public BaseJobGui(ITextComponent component) {
        super(component);

    }

    @Override
    public void init(Minecraft minecraft, int width, int height){
        super.init(minecraft,width,height);
       addButton(Done = new Button(width-120,height-30,110,20,"Done",(Done)->{
            minecraft.displayGuiScreen(null);
        }));
        addButton(Hire = new Button (20,height-60,110,20,"Hire",(Hire->{
            //TODO: Hiring COMPATIBILITY
        })));

        addButton(Fire =new Button (20,height-30,110,20,"Fire",(Fire->{
            //TODO: FIRING COMPATIBILITY
        })));
        addButton(ShowEmployees = new Button (width-120,height-60,110,20,"Show Employees",(ShowEmployees->{
        })));
        Fire.active = false;



    }
    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        super.render(p_render_1_,p_render_2_,p_render_3_);

    }

    @Override
    public void renderBackground(){
        super.renderBackground();
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    private void ShowHiring(){
        for(Widget button:buttons){
            button.visible = false;
        }

    }
}


