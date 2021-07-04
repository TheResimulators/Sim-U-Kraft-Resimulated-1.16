package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.enums.Category;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.packets.StartBuildingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class GuiBuilder extends GuiBaseJob {
    private Button Build;
    private Button CustomBack;
    private Button residential;
    private Button commercial;
    private Button industrial;
    private Button special;
    private Button nextPage;
    private Button previousPage;
    private Category currentCategory;
    private BuildingTemplate selected;
    private boolean loaded = false;
    private Button confirmBuilding;
    private ArrayList<BuildingTemplate> structures;
    private HashMap<Category, ArrayList<StructureButton>> structureButtons = new HashMap<>();
    private int maxButtons;
    private int pageIndex = 0;
    public GuiBuilder(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, @Nullable int id) {
        super(component, ids, pos, id, Profession.BUILDER.getId());
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        if (structures != null){
            loaded = true;
        }
        pageIndex = 0;

            addButton(Build = new LargeButton(width / 2 - 55, height - 55, 110, 42, new StringTextComponent("Build"), (Build -> {
                super.hideAll();
                CustomBack.visible = true;
                controlCategoryButtons(true);
                state = State.SELECTCATEGORY;
            })));
            //Build.active=false;
            addButton(CustomBack = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Back"), (Back -> {
                super.Back.onPress();
                if (state == State.SELECTBULDING) {
                    state = State.SELECTCATEGORY;
                    controlStructures(false,currentCategory);
                    controlCategoryButtons(true);
                    nextPage.visible = false;
                    previousPage.visible = false;

                }
                else if (state == State.BUILDINGINFO) {
                    state = State.SELECTBULDING;
                    nextPage.visible = true;
                    previousPage.visible = true;
                    controlStructures(true,currentCategory);
                    confirmBuilding.visible = false;
                }
                else if (state == State.SELECTCATEGORY){
                    state = State.MAIN;
                    controlCategoryButtons(false);
                    showMainMenu();
                }
            })));
            addButton(confirmBuilding = new Button(20, height - 30, 110, 20, new StringTextComponent("Confirm"), Confirm -> startBuilding()));
                confirmBuilding.visible = false;

            addButton(residential = new Button(width/2-100,height/2-30,100,20, new StringTextComponent("Residential"),Residential ->{
                controlCategoryButtons(false);
                currentCategory = Category.RESIDENTIAL;
                controlStructures(true,currentCategory);
                state = State.SELECTBULDING;
                nextPage.visible = true;
                previousPage.visible = true;
            }));
            addButton(commercial = new Button(width/2+10,height/2-30, 100, 20, new StringTextComponent("Commercial"),Commercial -> {
                controlCategoryButtons(false);
                currentCategory = Category.COMMERCIAL;
                controlStructures(true,currentCategory);
                state = State.SELECTBULDING;
                nextPage.visible = true;
                previousPage.visible = true;
            }));

            addButton(industrial = new Button(width/2-100,height/2+30,100,20,new StringTextComponent("Industrial"),industrial-> {
                controlCategoryButtons(false);
                currentCategory = Category.INDUSTRIAL;
                controlStructures(true,currentCategory);
                state = State.SELECTBULDING;
                nextPage.visible = true;
                previousPage.visible = true;
            }));

            addButton(special = new Button(width/2+10,height/2 + 30,100,20, new StringTextComponent("Special"),special ->{
                controlCategoryButtons(false);
                currentCategory = Category.SPECIAL;
                controlStructures(true,currentCategory);
                state = State.SELECTBULDING;
                nextPage.visible = true;
                previousPage.visible = true;
            }));

            addButton(nextPage = new Button(width-120,height-60,100,20, new StringTextComponent("Next Page"),nextPage ->{
                hideAllStructures(currentCategory);
                structureButtons.computeIfAbsent(currentCategory, k -> new ArrayList<>());
                if ((pageIndex + 1) * maxButtons < structureButtons.get(currentCategory).size())
                {pageIndex++;}
                controlStructures(true,currentCategory);
            }));

            addButton(previousPage = new Button(20,height-60,100,20, new StringTextComponent("Previous Page"), previousPage ->{
                hideAllStructures(currentCategory);
                pageIndex--;
                controlStructures(true,currentCategory);

            }));
            nextPage.visible = false;
            previousPage.visible = false;
            residential.visible = false;
            commercial.visible = false;
            industrial.visible = false;
            special.visible = false;
            if (structures != null && !loaded){
                createStructureButtons();
                loaded = true;

            }
            if (!isHired()) {
                Build.active = false;
            }
        if (!loaded) {
            CustomBack.visible = false;
            Build.visible = false;
        }else {
            CustomBack.visible = false;
            if (state != State.MAIN){
                Build.visible = false;

            if (state == State.SELECTBULDING){

                controlStructures(true,currentCategory);
                CustomBack.visible = true;
            }
            if (state == State.BUILDINGINFO){
                confirmBuilding.visible = true;
                CustomBack.visible = true;
            }
            if (state == State.SELECTCATEGORY){
                CustomBack.visible = true;
                controlCategoryButtons(true);
            }
            }

        }

    }

    private void startBuilding() {
        Network.getNetwork().sendToServer(new StartBuildingPacket(pos,Minecraft.getInstance().player.getMotionDirection(),selected.getName(),Minecraft.getInstance().player.getUUID()));
        Minecraft.getInstance().setScreen(null);
    }

    public void setStructures(ArrayList<BuildingTemplate> structures) {
        this.loaded = true;
        Build.visible = true;
        this.structures = structures;
        createStructureButtons();

    }

    public void createStructureButtons(){
        int xSpacing = 150;
        int xPadding = 20;
        int maxButtonsWidth = this.width/150;
        maxButtons = maxButtonsWidth * (this.height/125);
        int index;
        for (BuildingTemplate template: structures) {
            BuildingType type = BuildingType.getById(template.getTypeID());

            if (type == null){
                type = BuildingType.SPECIAL;
                SimuKraft.LOGGER().error("structure " + template.getName() + " is missing building type and Has been added as a special building");
            }
            StructureButton button = new StructureButton();
            structureButtons.computeIfAbsent(type.category, k -> new ArrayList<>());
            index = (structureButtons.get(type.category)).size() %maxButtons;
            button.createButtons(template,(xSpacing *(index%maxButtonsWidth) + xPadding),100 * (((int)index/maxButtonsWidth)) + 25);
            ArrayList<StructureButton> list = structureButtons.get(type.category);
            list.add(button);
            structureButtons.put(type.category,list);

        }


    }

    private void controlCategoryButtons(boolean enable){
        residential.visible = enable;
        commercial.visible = enable;
        industrial.visible = enable;
        special.visible = enable;

    }

    private void controlStructures(boolean visible, Category category){
            if (structureButtons.containsKey(category)){
                int currentIndex = maxButtons*pageIndex;
                if (currentIndex < 0){
                    currentIndex = 0;
                    pageIndex = 0;
                }
                for (int i = currentIndex; i<maxButtons + currentIndex;i++){
                    if (i >= structureButtons.get(category).size()) return;
                    StructureButton button = structureButtons.get(category).get(i);
                    if (!buttons.contains(button.name)){
                        button.addButtonsToGui();
                    }
                    button.controlVisibility(visible);
                    System.out.println(button.getVisibility());
                }
        }
    }
    private void hideAllStructures(Category category){
        if(structureButtons.containsKey(category)){
            for(StructureButton button: structureButtons.get(category)){
                button.controlVisibility(false);
            }

        }

    }
    private void controlStructures(boolean visible){
        for (Category category: Category.values()){
            controlStructures(visible,category);
        }

    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack);
        if (loaded) {
            super.render(stack, p_render_1_, p_render_2_, p_render_3_);
            if (state == State.BUILDINGINFO){
                font.draw(stack, "Building Name: " + selected.getName(), (float) width / 6, (float) height / 4, Color.WHITE.getRGB());
                font.draw(stack, "Author: " + selected.getAuthor(), (float) width / 6, (float) height / 4+20, Color.WHITE.getRGB());
                font.draw(stack, "Price: " + selected.getCost(), (float) width / 6, (float) height / 4+40, Color.WHITE.getRGB());
                font.draw(stack, "Rent: " + selected.getRent(), (float) width / 6 , (float) height / 4+60, Color.WHITE.getRGB());

            }





        }
        else {
            font.draw(stack, "Loading", (float) width / 2 - font.width("Loading") / 2, (float) height / 2, Color.WHITE.getRGB());
        }

    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
        Build.visible = true;
    }

    private static class State extends GuiBaseJob.State {
        private static final int SELECTBULDING = nextID();
        private static final int BUILDINGINFO = nextID();
        private static final int SELECTCATEGORY = nextID();



    }

    private class StructureButton {
        ArrayList<Button> infoButtons = new ArrayList<>();
        Button name;
        Button price;
        Button author;
        Button rent;
        int width = 150;
        int height = 20;

        void createButtons(BuildingTemplate template, int x, int y){
        try {
            name = new Button(x, y, width, height, new StringTextComponent(template.getName()), button -> {
                state = State.BUILDINGINFO;
                CustomBack.visible = true;
                confirmBuilding.visible = true;
                nextPage.visible = false;
                previousPage.visible = false;
                controlStructures(false);
                selected = template;
            });
            name.visible = false;
            author = new Button(x, y + height, width, height, new StringTextComponent("Author: " + template.getAuthor()), button -> {
            });
            author.active = false;
            author.visible = false;
            price = new Button(x, y + height * 2, width, height, new StringTextComponent("Price: " + template.getCost()), button -> {
            });
            price.active = false;
            price.visible = false;
            rent = new Button(x, y + height * 3, width, height, new StringTextComponent("Rent: " + template.getRent()), button -> {
            });
            rent.active = false;
            rent.visible = false;
            infoButtons.add(name);
            infoButtons.add(author);
            infoButtons.add(price);
            infoButtons.add(rent);
            addButtonsToGui();
        } catch (Exception e){
            e.printStackTrace();

        }
        }

        void addButtonsToGui(){
            addButton(name);
            addButton(author);
            addButton(price);
            addButton(rent);

        }
        void controlVisibility(boolean visible){
            name.visible = visible;
            author.visible = visible;
            price.visible = visible;
            rent.visible = visible;
        }

        boolean getVisibility(){
            return name.visible;
        }
    }

    private class LargeButton extends Button {
        final ResourceLocation LARGE_BUTTON = new ResourceLocation(Reference.MODID, "textures/gui/large_button.png");

        public LargeButton(int widthIn, int heightIn, int width, int height, ITextComponent text, IPressable onPress) {
            super(widthIn, heightIn, width, height, text, onPress);
        }

        @Override
        public void renderButton(MatrixStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.font;
            minecraft.getTextureManager().bind(LARGE_BUTTON);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.blit(stack, this.x, this.y, 0, 1 + i * 42, this.width / 2, this.height);
            this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 1 + i * 42, this.width / 2, this.height);
            this.renderBg(stack, minecraft, p_renderButton_1_, p_renderButton_2_);
            int j = getFGColor();
            drawCenteredString(stack, fontrenderer, this.getMessage().getString(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

        }


        @Override
        public int getYImage(boolean hovered) {
            int i = 0;
            if (!this.active) {
                i = 1;
            } else if (hovered) {
                i = 2;
            }

            return i;
        }
    }
}
