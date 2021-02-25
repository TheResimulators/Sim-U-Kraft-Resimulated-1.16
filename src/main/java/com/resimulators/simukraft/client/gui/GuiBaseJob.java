package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.SimFireRequest;
import com.resimulators.simukraft.packets.SimHireRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class GuiBaseJob extends Screen {
    Button Hire;
    Button Fire;
    Button ShowEmployees;
    Button Done;
    Button Back;
    Button Confirm;
    PlayerEntity player;
    ArrayList<Integer> ids;
    ArrayList<SimButton> simButtons = new ArrayList<>();
    SimEntity selectedSim;
    ITextComponent component;
    int state = State.MAIN;
    BlockPos pos;
    boolean firing = false;
    int hiredId;
    int job;
    ArrayList<Button> mainMenu = new ArrayList<Button>() {{
    }};

    public GuiBaseJob(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id, int job) {
        super(component);
        this.hiredId = id;
        this.player = Minecraft.getInstance().player;
        this.ids = ids;
        this.pos = pos;
        this.job = job;
        this.component = component;
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        addButton(Done = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Done"), (Done) -> minecraft.displayGuiScreen(null)));

        addButton(Hire = new Button(20, height - 60, 110, 20, new StringTextComponent("Hire"), (Hire -> {
            ShowHiring();
            state = State.HIRE_INFO;  //hire_info is used to select a sim for hiring
        })));

        addButton(Fire = new Button(20, height - 30, 110, 20, new StringTextComponent("Fire"), (Fire -> {
            firing = true;
            showFiring();
        })));
        addButton(ShowEmployees = new Button(width - 120, height - 60, 110, 20, new StringTextComponent("Show Employees"), (ShowEmployees -> {
            hideAll();
            ShowEmployees();
            state = State.SHOW_EMPLOYEES;
        })));
        ITile tile = ((ITile) SimuKraft.proxy.getClientWorld().getTileEntity(pos));
        if (tile != null) {
            if (tile.getHired()) {
                Fire.active = true;
                Hire.active = false;
            } else {
                Fire.active = false;
                Hire.active = true;
            }
        }

        addButton(Back = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Back"), (Back -> {
            if (state == State.HIRE_INFO) {
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SHOW_EMPLOYEES) {
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SIM_EMPLOYEE_INFO) {
                state = State.SHOW_EMPLOYEES;
                ShowEmployees();
            }
            if (state == State.SIM_HIRING_INFO) {
                state = State.HIRE_INFO;
                ShowHiring();
            }
            if (state == State.Firing) {
                state = State.MAIN;
                showMainMenu();
            }

        })));
        Back.visible = false;


        addButton(Confirm = new Button(20, height - 30, 110, 20, new StringTextComponent("Confirm"), Confirm -> sendPackets()));
        Confirm.visible = false;
        if (state == State.HIRE_INFO) {
            ShowHiring();
            Hire.visible = false;
            Fire.visible = false;
            ShowEmployees.visible = false;
        }
        if (state != State.MAIN){
            hideAll();
        }
        mainMenu.add(Hire);
        mainMenu.add(Fire);
        mainMenu.add(ShowEmployees);
        mainMenu.add(Done);
    }

    public void showMainMenu() {
        hideAll();
        for (Button button:mainMenu){
            button.visible = true;
        }
    }
    public void hideMainMenu(){
        for (Button button:mainMenu){
            button.visible = false;
        }

    }
    private void showFiring() {
        hideAll();
        state = State.Firing;
        Confirm.visible = true;
        Back.visible = true;


    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack); //Render Background
        super.render(stack, p_render_1_, p_render_2_, p_render_3_);

        if (state == State.MAIN) {

            if (isHired()) {
                font.drawString(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
                if (hiredId != 0) {
                    font.drawString(stack, "Name: " + Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
                }
            }
        } else if (state == State.HIRE_INFO) {
            font.drawString(stack, "Hiring", (float) (width / 2 - font.getStringWidth("Hiring") / 2), 10, Color.white.getRGB());
        } else if (state == State.SIM_HIRING_INFO || state == State.SIM_EMPLOYEE_INFO) {
            font.drawString(stack, "Info", (float) (width / 2 - font.getStringWidth("Info") / 2), 10, Color.white.getRGB());
            font.drawString(stack, "Name: " + selectedSim.getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
            font.drawString(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
        } else if (state == State.SHOW_EMPLOYEES) {
            font.drawString(stack, "Employees", (float) (width / 2 - font.getStringWidth("Employees") / 2), 10, Color.white.getRGB());
        }
        if (state == State.Firing) {
            font.drawString(stack, "Level: WIP", (float) 20, 70, Color.white.getRGB());
            if (hiredId != 0) {
                font.drawString(stack, "Name: " + Minecraft.getInstance().world.getEntityByID(hiredId).getDisplayName().getString(), (float) 20, 50, Color.white.getRGB());
            }
        }

    }

    private void ShowHiring() {
        hideAll();

        Back.visible = true;
        int x = 0;
        int y = 0;
        int ConstantXSpacing = (width / 5);
        int ConstantYSpacing = height / 4;
        for (Integer id : ids) {
            SimEntity sim = (SimEntity) player.getEntityWorld().getEntityByID(id);
            if (sim != null) {
                UUID uuid = sim.getUniqueID();
                SavedWorldData data = SavedWorldData.get(player.world);
                int Id = data.getFactionWithPlayer(player.getUniqueID()).getId();
                if (!data.getFaction(Id).getHired(uuid)) {
                    simButtons.add(addButton(new SimButton(20 + x * ConstantXSpacing, 40 + y * ConstantYSpacing, 100, 20, sim.getName(), id, this, 0)));
                    x++;
                    if (x > 4) {
                        x = 0;
                        y++;
                    }
                }
            }
        }
    }

    private void ShowEmployees() {


        Back.visible = true;
        int x = 0;
        int y = 0;
        int ConstantXSpacing = (width / 5);
        int ConstantYSpacing = height / 4;
        for (Integer id : ids) {
            SimEntity sim = (SimEntity) player.getEntityWorld().getEntityByID(id);
            if (sim != null) {
                UUID uuid = sim.getUniqueID();
                SavedWorldData data = SavedWorldData.get(player.world);
                int Id = data.getFactionWithPlayer(player.getUniqueID()).getId();
                if (data.getFaction(Id).getHired(uuid)) {
                    simButtons.add(addButton(new SimButton(20 + x * ConstantXSpacing, 40 + y * ConstantYSpacing, 100, 20, sim.getName(), id, this, 1)));
                    x++;
                    if (x > 4) {
                        x = 0;
                        y++;
                    }
                }
            }
        }
    }

    protected void hideAll() {
        for (Widget button : buttons) {
            button.visible = false;
        }
    }

    private void showSimInfo(int id) {
        hideAll();
        selectedSim = (SimEntity) player.world.getEntityByID(id);
        state = State.SIM_HIRING_INFO;
        Back.visible = true;
        Confirm.visible = true;
    }

    private void showEmployeeInfo(int id) {
        hideAll();
        selectedSim = (SimEntity) player.world.getEntityByID(id);
        state = State.SIM_EMPLOYEE_INFO;
        Back.visible = true;
        Confirm.visible = false;
    }



    static class SimButton extends Button {

        SimButton(int widthIn, int heightIn, int width, int height, ITextComponent text, int id, GuiBaseJob gui, int confirmButton) {
            super(widthIn, heightIn, width, height, text, (Sim -> {
                switch (confirmButton) {
                    case 0: {
                        gui.showSimInfo(id);
                        break;
                    }
                    case 1: {
                        gui.showEmployeeInfo(id);
                        break;
                    }
                    // For more button add cases
                }
            }));
        }
    }

    public boolean isHired() {
        if (Minecraft.getInstance().world != null){
        ITile tile = (ITile) Minecraft.getInstance().world.getTileEntity(pos);
        if (tile != null) return (tile.getSimId() != null);
        }
        return false;
    }
    /**sends different packets to server to request different things*/
    private void sendPackets() {
        if (!firing) {
            Network.getNetwork().sendToServer(new SimHireRequest(selectedSim.getEntityId(), Minecraft.getInstance().player.getUniqueID(), pos, job));
        } else {
            Network.getNetwork().sendToServer(new SimFireRequest(Minecraft.getInstance().player.getUniqueID(), ((ITile) Minecraft.getInstance().world.getTileEntity(pos)).getSimId(), pos));
        }
        Minecraft.getInstance().displayGuiScreen(null);
    }

    protected static class State {
        protected static int id = nextID();
        protected static int MAIN = nextID();
        protected static int SIM_HIRING_INFO = nextID();
        protected static int HIRE_INFO = nextID();
        protected static int SIM_EMPLOYEE_INFO = nextID();
        protected static int SHOW_EMPLOYEES = nextID();
        protected static int Firing = nextID();


        protected static int nextID() {
            return id++;
        }


    }
}


