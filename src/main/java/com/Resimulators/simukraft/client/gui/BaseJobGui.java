package com.Resimulators.simukraft.client.gui;

import com.Resimulators.simukraft.Network;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.packets.SimFirePacket;
import com.Resimulators.simukraft.packets.SimFireRequest;
import com.Resimulators.simukraft.packets.SimHireRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class BaseJobGui extends Screen {
    private Button Hire;
    private Button Fire;
    private Button ShowEmployees;
    private Button Done;
    private Button Back;
    private Button Confirm;
    private PlayerEntity player;
    private ArrayList<Integer> ids;
    private ArrayList<SimButton> simButtons = new ArrayList<>();
    private EntitySim selectedsim;
    private State state = State.MAIN;
    private BlockPos pos;
    private boolean firing = false;
    private int hiredId;

    
    public BaseJobGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos,@Nullable int id) {
        super(component);
        this.hiredId = id;
        this.player = Minecraft.getInstance().player;
        this.ids = ids;
        this.pos = pos;

    }

    @Override
    public void init(Minecraft minecraft, int width, int height){
        super.init(minecraft,width,height);
       addButton(Done = new Button(width-120,height-30,110,20,"Done",(Done)->{
            minecraft.displayGuiScreen(null);
        }));

        addButton(Hire = new Button (20,height-60,110,20,"Hire",(Hire->{
          ShowHiring();
          state = State.HIRE_INFO;//hire_info is used to select a sim for hiring
        })));

        addButton(Fire =new Button (20,height-30,110,20,"Fire",(Fire->{
            firing = true;
            showFiring();

        })));
        addButton(ShowEmployees = new Button (width-120,height-60,110,20,"Show Employees",(ShowEmployees->{
        })));
        Minecraft.getInstance().world.getTileEntity(pos);
        if (((ITile)Minecraft.getInstance().world.getTileEntity(pos)).getHired()){
            Fire.active = true;
            Hire.active = false;
        }else{
        Fire.active = false;
        Hire.active = true;
        }

        addButton(Back = new Button(width-120,height-30,110,20,"Back",(Back ->{
            if (state == State.HIRE_INFO){
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SHOW_EMPLOYEES){
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SIM_INFO){
                state= State.HIRE_INFO;
                ShowHiring();
            }
            if (state == State.Firing){
                state = State.MAIN;
                showMainMenu();
            }

        })));
        Back.visible = false;


        addButton(Confirm = new Button(20,height-30,110,20,"Confirm",Confirm ->{
           sendPackets();


        }));
        Confirm.visible = false;
        if (state == State.HIRE_INFO){
            ShowHiring();
            Hire.visible = false;
            Fire.visible = false;
            ShowEmployees.visible = false;
        }


    }

    public void showMainMenu(){
        hideAll();
        Hire.visible = true;
        Fire.visible = true;
        ShowEmployees.visible = true;
        Done.visible = true;


    }
    private void showFiring(){
        hideAll();
        Confirm.visible = true;
        Back.visible = true;

    }
    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        super.render(p_render_1_,p_render_2_,p_render_3_);
        if (state == State.MAIN){
            if (isHired()){
                font.drawString("Level: WIP",(float)20,70,Color.white.getRGB());
                if (hiredId != 0){
                font.drawString("Name: "+ Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getFormattedText(),(float)20,50,Color.white.getRGB());}
            }
        }else if (state == State.HIRE_INFO){
            font.drawString("Hiring",(float)(width/2-font.getStringWidth("Hiring")/2),10, Color.white.getRGB());
        }else if (state == State.SIM_INFO){
            font.drawString("Info",(float)(width/2-font.getStringWidth("Info")/2),10, Color.white.getRGB());
            font.drawString("Name: "+selectedsim.getDisplayName().getFormattedText(),(float)20,50,Color.white.getRGB());
            font.drawString("Level: WIP",(float)20,70,Color.white.getRGB());
        }
        if (state == State.Firing){
            font.drawString("Level: WIP",(float)20,70,Color.white.getRGB());
            if (hiredId != 0){
                font.drawString("Name: "+ Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getFormattedText(),(float)20,50,Color.white.getRGB());}
        }

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
        hideAll();

        Back.visible = true;
        int x = 0;
        int y = 0;
        int ConstantXSpacing = (width/5)*2;
        int ConstantYSpacing = height/4;
        for (int i = 0;i<ids.size();i++){
            EntitySim sim = (EntitySim)player.getEntityWorld().getEntityByID(ids.get(i));
            simButtons.add(addButton(new SimButton(20 + x*ConstantXSpacing,40 + y*ConstantYSpacing,100,20,sim.getName().getFormattedText(),ids.get(i),this)));
            x++;
            if (x >4){
                x = 0;
                y++;
            }

        }

    }
    private void hideAll(){
        for(Widget button:buttons){
            button.visible = false;
        }
    }

    private void showSimInfo(int id){
        hideAll();
        EntitySim sim = (EntitySim)player.world.getEntityByID(id);
        selectedsim = sim;
        state = State.SIM_INFO;
        Back.visible = true;
        Confirm.visible = true;

    }



    static class SimButton extends Button{
        private int id;
        SimButton(int widthIn, int heightIn, int width, int height, String text,int id,BaseJobGui gui) {
            super(widthIn, heightIn, width, height, text, (Sim ->{
                gui.showSimInfo(id);


            }));
            this.id = id;
        }


    }
    public boolean isHired(){
        return ((ITile)Minecraft.getInstance().world.getTileEntity(pos)).getSimId() != null;
    }

    private void sendPackets(){
        if (!firing) {
            Network.getNetwork().sendToServer(new SimHireRequest(selectedsim.getEntityId(), Minecraft.getInstance().player.getUniqueID(),pos));
        }else{
            Network.getNetwork().sendToServer(new SimFireRequest(Minecraft.getInstance().player.getUniqueID(),((ITile)Minecraft.getInstance().world.getTileEntity(pos)).getSimId(),pos));
        }
        Minecraft.getInstance().displayGuiScreen(null);
    }

    private enum State{
        MAIN,
        SIM_INFO,
        HIRE_INFO,
        SHOW_EMPLOYEES,
        Firing







    }
}


