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
    Button nextPage;
    Button previousPage;
    PlayerEntity player;
    ArrayList<Integer> ids;
    ArrayList<SimButton> simButtons = new ArrayList<>();
    ArrayList<SimButton> employeeButtons = new ArrayList<>();
    SimEntity selectedSim;
    ITextComponent component;
    int state = State.MAIN;
    BlockPos pos;
    boolean firing = false;
    int hiredId;
    int job;
    private int pageIndex;
    private int maxButtons;
    private final SavedWorldData data;
    private final int Id;
    ArrayList<Button> mainMenu = new ArrayList<Button>() {{
    }};

    public GuiBaseJob(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id, int job) {
        super(component);
        this.hiredId = id;
        this.player = Minecraft.getInstance().player;
        data = SavedWorldData.get(player.world);
        Id = data.getFactionWithPlayer(player.getUniqueID()).getId();
        this.ids = ids;
        this.pos = pos;
        this.job = job;
        this.component = component;
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        employeeButtons.clear();
        simButtons.clear();
        pageIndex = 0;
        createEmployees();
        addButton(Done = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Done"), (Done) -> minecraft.displayGuiScreen(null)));

        addButton(Hire = new Button(20, height - 60, 110, 20, new StringTextComponent("Hire"), (Hire -> {
            pageIndex = 0;
            hideAll();
            showHiring();
            Back.visible = true;
            nextPage.visible = true;
            previousPage.visible = true;
            state = State.HIRE_INFO;  //hire_info is used to select a sim for hiring
        })));
        createHiring();
        addButton(Fire = new Button(20, height - 30, 110, 20, new StringTextComponent("Fire"), (Fire -> {
            firing = true;
            showFiring();
        })));
        addButton(ShowEmployees = new Button(width - 120, height - 60, 110, 20, new StringTextComponent("Show Employees"), (ShowEmployees -> {
            pageIndex = 0;
            hideAll();
            showEmployees();
            Back.visible = true;
            nextPage.visible = true;
            previousPage.visible = true;
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
                previousPage.visible = true;
                nextPage.visible = true;
                state = State.SHOW_EMPLOYEES;
                showEmployees();
            }
            if (state == State.SIM_HIRING_INFO) {
                previousPage.visible = true;
                nextPage.visible = true;
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
            Hire.visible = false;
            Fire.visible = false;
            ShowEmployees.visible = false;
        }

        addButton(nextPage = new Button(width-120,height-60,100,20, new StringTextComponent("Next Page"),nextPage ->{

            if (state == State.SHOW_EMPLOYEES){
                if ((pageIndex + 1) * maxButtons < employeeButtons.size())
                {pageIndex++;}
                hideEmployees();
                showEmployees();
            }else if (state == State.HIRE_INFO){
                if ((pageIndex + 1) * maxButtons < simButtons.size())
                {pageIndex++;}
                hideHiring();
                showHiring();
            }

        }));

        addButton(previousPage = new Button(20,height-60,100,20, new StringTextComponent("Previous Page"), previousPage ->{
            pageIndex--;
            if (state == State.SHOW_EMPLOYEES){
                hideEmployees();
                showEmployees();
            }else if (state == State.HIRE_INFO){
                hideHiring();
                showHiring();
            }
        }));

        previousPage.visible = false;
        nextPage.visible = false;
        Back.visible = false;
        if (state != State.MAIN){
            hideAll();
            showHiring();
            Back.visible = true;
            nextPage.visible = true;
            previousPage.visible = true;
        }
        if (state == State.HIRE_INFO){
            showHiring();
            Back.visible = true;
            nextPage.visible = true;
            previousPage.visible = true;
        }
        if (state == State.SHOW_EMPLOYEES){
            showEmployees();
            Back.visible = true;
            nextPage.visible = true;
            previousPage.visible = true;
        }

        mainMenu.add(Hire);
        mainMenu.add(Fire);
        mainMenu.add(ShowEmployees);
        mainMenu.add(Done);


    }

    private void ShowHiring() {
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

    private void showHiring(){
        int currentIndex = maxButtons*pageIndex;
        if (currentIndex < 0){
            currentIndex = 0;
            pageIndex = 0;
        }
        for (int i = currentIndex; i<maxButtons + currentIndex;i++){
            if (i >= simButtons.size()) return;
            SimButton button = simButtons.get(i);
            button.visible = true;
        }


    }

    private void showEmployees(){
        int currentIndex = maxButtons*pageIndex;
        if (currentIndex < 0){
            currentIndex = 0;
            pageIndex = 0;
        }
        for (int i = currentIndex; i<maxButtons + currentIndex;i++){
            if (i >= employeeButtons.size()) return;
            SimButton button = employeeButtons.get(i);
            button.visible = true;
        }
    }

    private void hideHiring(){
        for(SimButton button: simButtons){
            button.visible = false;
        }

    }

    private void hideEmployees(){
        for(SimButton button: employeeButtons){
            button.visible = false;
        }

    }

    private void createHiring() {

        int ConstantXSpacing = 125;
        int ConstantYSpacing = 30;
        int maxWidth = width/125;
        int i = 0;
        maxButtons = maxWidth * (height-150)/30;
        for (int id: ids) {
            SimEntity sim = (SimEntity) player.getEntityWorld().getEntityByID(id);
            if (sim != null) {
                UUID uuid = sim.getUniqueID();
                int index = i% maxButtons;
                if (!data.getFaction(Id).getHired(uuid)) {
                    SimButton button = new SimButton(ConstantXSpacing*(index%maxWidth) + 40,   ConstantYSpacing * (index/maxWidth) + 40, 100, 20, sim.getName(), ids.get(i), this, 0);
                    simButtons.add(addButton(button));
                    button.visible = false;
                    i++;
                }
            }

        }
    }

    private void createEmployees() {
        int ConstantXSpacing = 125;
        int ConstantYSpacing = 30;
        int maxWidth = width/125;
        int i = 0;
        maxButtons = maxWidth * height/30;
        for (int id: ids) {
            SimEntity sim = (SimEntity) player.getEntityWorld().getEntityByID(id);
            if (sim != null) {
                UUID uuid = sim.getUniqueID();
                int index = i% maxButtons;
                if (data.getFaction(Id).getHired(uuid)) {
                    SimButton button = new SimButton(ConstantXSpacing*(index%maxWidth) + 20,   ConstantYSpacing * (index/maxWidth) + 40, 100, 20, sim.getName(), ids.get(i), this, 1);
                    employeeButtons.add(addButton(button));
                    button.visible = false;
                    i++;
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


