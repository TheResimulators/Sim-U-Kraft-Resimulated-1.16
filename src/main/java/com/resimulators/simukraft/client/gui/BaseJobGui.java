package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.packets.SimFireRequest;
import com.resimulators.simukraft.packets.SimHireRequest;
import com.sun.java.accessibility.util.java.awt.TextComponentTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class BaseJobGui extends Screen {
    Button Hire;
    Button Fire;
    Button ShowEmployees;
    Button Done;
    Button Back;
    Button Confirm;
    PlayerEntity player;
    ArrayList<Integer> ids;
    ArrayList<SimButton> simButtons = new ArrayList<>();
    SimEntity selectedsim;
    int state = State.MAIN;
    BlockPos pos;
    boolean firing = false;
    int hiredId;
    int job;

    public BaseJobGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id, int job) {
        super(component);
        this.hiredId = id;
        this.player = Minecraft.getInstance().player;
        this.ids = ids;
        this.pos = pos;
        this.job = job;
    }

    @Override
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);
        func_230480_a_(Done = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Done"), (Done) -> minecraft.displayGuiScreen(null)));

        func_230480_a_(Hire = new Button(20, height - 60, 110, 20, new StringTextComponent("Hire"), (Hire -> {
            ShowHiring();
            state = State.HIRE_INFO;//hire_info is used to select a sim for hiring
        })));

        func_230480_a_(Fire = new Button(20, height - 30, 110, 20, new StringTextComponent("Fire"), (Fire -> {
            firing = true;
            showFiring();
        })));
        func_230480_a_(ShowEmployees = new Button(width - 120, height - 60, 110, 20, new StringTextComponent("Show Employees"), (ShowEmployees -> {
            //TODO: Implement show employes system to show all employees that have a job
        })));
        Minecraft.getInstance().world.getTileEntity(pos);
        if (((ITile) Minecraft.getInstance().world.getTileEntity(pos)).getHired()) {
            Fire.field_230693_o_ = true;
            Hire.field_230693_o_ = false;
        } else {
            Fire.field_230693_o_ = false;
            Hire.field_230693_o_ = true;
        }

        func_230480_a_(Back = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Back"), (Back -> {
            if (state == State.HIRE_INFO) {
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SHOW_EMPLOYEES) {
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SIM_INFO) {
                state = State.HIRE_INFO;
                ShowHiring();
            }
            if (state == State.Firing) {
                state = State.MAIN;
                showMainMenu();
            }

        })));
        Back.field_230694_p_ = false;


        func_230480_a_(Confirm = new Button(20, height - 30, 110, 20, new StringTextComponent("Confirm"), Confirm -> sendPackets()));
        Confirm.field_230694_p_ = false;
        if (state == State.HIRE_INFO) {
            ShowHiring();
            Hire.field_230694_p_ = false;
            Fire.field_230694_p_ = false;
            ShowEmployees.field_230694_p_ = false;
        }


    }

    public void showMainMenu() {
        hideAll();
        Hire.field_230694_p_ = true;
        Fire.field_230694_p_ = true;
        ShowEmployees.field_230694_p_ = true;
        Done.field_230694_p_ = true;
    }

    private void showFiring() {
        hideAll();
        state = State.Firing;
        Confirm.field_230694_p_ = true;
        Back.field_230694_p_ = true;


    }

    @Override
    public void func_230430_a_(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        func_230446_a_(stack); //Render Background
        super.func_230430_a_(stack, p_render_1_, p_render_2_, p_render_3_);
        if (state == State.MAIN) {
            if (isHired()) {
                field_230712_o_.func_238421_b_(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
                if (hiredId != 0) {
                    field_230712_o_.func_238421_b_(stack, "Name: " + Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
                }
            }
        } else if (state == State.HIRE_INFO) {
            field_230712_o_.func_238421_b_(stack, "Hiring", (float) (field_230708_k_ / 2 - field_230712_o_.getStringWidth("Hiring") / 2), 10, Color.white.getRGB());
        } else if (state == State.SIM_INFO) {
            field_230712_o_.func_238421_b_(stack, "Info", (float) (field_230708_k_ / 2 - field_230712_o_.getStringWidth("Info") / 2), 10, Color.white.getRGB());
            field_230712_o_.func_238421_b_(stack, "Name: " + selectedsim.getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
            field_230712_o_.func_238421_b_(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
        }
        if (state == State.Firing) {
            field_230712_o_.func_238421_b_(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
            if (hiredId != 0) {
                field_230712_o_.func_238421_b_(stack, "Name: " + Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
            }
        }

    }

    private void ShowHiring() {
        hideAll();

        Back.field_230694_p_ = true;
        int x = 0;
        int y = 0;
        int ConstantXSpacing = (field_230708_k_ / 5) * 2;
        int ConstantYSpacing = field_230709_l_ / 4;
        for (int i = 0; i < ids.size(); i++) {
            SimEntity sim = (SimEntity) player.getEntityWorld().getEntityByID(ids.get(i));
            if (sim != null) {
                simButtons.add(func_230480_a_(new SimButton(20 + x * ConstantXSpacing, 40 + y * ConstantYSpacing, 100, 20, sim.getName(), ids.get(i), this)));
                x++;
                if (x > 4) {
                    x = 0;
                    y++;
                }
            }
        }

    }

    protected void hideAll() {
        for (Widget button : field_230710_m_) {
            button.field_230694_p_ = false;
        }
    }

    private void showSimInfo(int id) {
        hideAll();
        SimEntity sim = (SimEntity) player.world.getEntityByID(id);
        selectedsim = sim;
        state = State.SIM_INFO;
        Back.field_230694_p_ = true;
        Confirm.field_230694_p_ = true;

    }


    static class SimButton extends Button {
        private int id;

        SimButton(int widthIn, int heightIn, int width, int height, ITextComponent text, int id, BaseJobGui gui) {
            super(widthIn, heightIn, width, height, text, (Sim -> {
                gui.showSimInfo(id);


            }));
            this.id = id;
        }


    }

    public boolean isHired() {
        return ((ITile) Minecraft.getInstance().world.getTileEntity(pos)).getSimId() != null;
    }

    private void sendPackets() {
        if (!firing) {
            Network.getNetwork().sendToServer(new SimHireRequest(selectedsim.getEntityId(), Minecraft.getInstance().player.getUniqueID(), pos, job));
        } else {
            Network.getNetwork().sendToServer(new SimFireRequest(Minecraft.getInstance().player.getUniqueID(), ((ITile) Minecraft.getInstance().world.getTileEntity(pos)).getSimId(), pos));
        }
        Minecraft.getInstance().displayGuiScreen(null);
    }

    protected static class State {
        protected static int id = 0;
        protected static int MAIN = nextID();
        protected static int SIM_INFO = nextID();
        protected static int HIRE_INFO = nextID();
        protected static int SHOW_EMPLOYEES = nextID();
        protected static int Firing = nextID();


        protected static int nextID() {
            return id++;
        }


    }
}


