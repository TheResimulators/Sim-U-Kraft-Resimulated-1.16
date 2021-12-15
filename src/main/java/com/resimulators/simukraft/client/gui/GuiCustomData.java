package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.item.ItemStructureTest;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.packets.CustomDataSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;

public class GuiCustomData extends Screen {
    private final BlockPos pos;
    private final ItemStack stack;
    private final ArrayList<Button> buildingTypes = new ArrayList<>();
    private final TileCustomData tile;
    private Button done;
    private Button calculatePrice;
    private Button calculateRent;
    private TextFieldWidget rentInput;
    private TextFieldWidget priceInput;
    private ButtonScrollPanel buildingTypePanel;
    private int buildingWidth;
    private int buildingHeight;
    private int buildingDepth;

    protected GuiCustomData(ITextComponent titleIn, BlockPos pos) {
        super(titleIn);
        this.pos = pos;
        stack = Minecraft.getInstance().player.getMainHandItem();
        if ((stack.getItem() != Items.AIR)) {
            //set width height and depth
        }

        tile = (TileCustomData) SimuKraft.proxy.getClientWorld().getBlockEntity(pos);
        if (tile != null) {
            buildingHeight = tile.getHeight();
            buildingWidth = tile.getWidth();
            buildingDepth = tile.getDepth();

        }
    }


    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack); //Render Background
        buildingTypePanel.render(stack, p_render_1_, p_render_2_, p_render_3_);

        super.render(stack, p_render_1_, p_render_2_, p_render_3_);

        getMinecraft().font.draw(stack, "Currently Selected", width / 2 - 40, height / 2 - 100, Color.WHITE.getRGB());
        if (buildingTypePanel.selection.string != null) {
            minecraft.font.draw(stack, "Building: " + StringUtils.capitalize(buildingTypePanel.selection.string), width / 2 - 40, height / 2 - 80, Color.WHITE.getRGB());
        }
        priceInput.render(stack, p_render_1_, p_render_2_, p_render_3_);
        rentInput.render(stack, p_render_1_, p_render_2_, p_render_3_);
        rentInput.renderButton(stack, p_render_1_, p_render_2_, p_render_3_);

        if ((buildingWidth != 0 && buildingHeight != 0 && buildingDepth != 0) || rentInput != null) {
            minecraft.font.draw(stack, ("Width: " + buildingWidth), width / 2 - 40, height / 2 - 40, Color.WHITE.getRGB());
            minecraft.font.draw(stack, ("Height: " + buildingHeight), width / 2 - 40, height / 2 - 20, Color.WHITE.getRGB());
            minecraft.font.draw(stack, ("Depth: " + buildingDepth), width / 2 - 40, height / 2, Color.WHITE.getRGB());

        }

        if (buildingTypePanel.selection.getType() != null) {
            String category = buildingTypePanel.selection.getType().category.category;
            minecraft.font.draw(stack, ("Category: " + StringUtils.capitalize(category)), width / 2 - 40, height / 2 - 60, Color.WHITE.getRGB());
        }


    }

    @Override
    public void onClose() {
        super.onClose();
        UpdateServer();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        addButton(done = new Button(width / 2 - 100, (height / 4) * 3 + 30, 200, 20, new StringTextComponent("Done"), done -> {
            onClose();
        }));

        addButton(calculatePrice = new Button(width - 120, height / 2 - 30, 100, 20, new StringTextComponent("Calculate Price"), priceCalculate -> {
            if (stack.getItem() instanceof ItemStructureTest) {
                //TODO: calculate price depending on the size of the structure

            }
        }));

        addButton(calculateRent = new Button(width - 120, (height / 2) + 50, 100, 20, new StringTextComponent("Calculate Rent"), calculateRent -> {
            if (stack.getItem() instanceof ItemStructureTest) {
                //TODO: calculate rent depending on the size of the structure
            }
        }));
        buildingTypes.clear();
        addBuildingTypeButtons();
        buildingTypePanel = new ButtonScrollPanel(minecraft, 108, 120, height / 2 - 100, 30, buildingTypes, buildingTypes.size(), 60);

        this.children.add(buildingTypePanel);

        rentInput = new TextFieldWidget(minecraft.font, width - 120, (height / 2) + 80, 100, 20, new StringTextComponent("Rent"));
        priceInput = new TextFieldWidget(minecraft.font, width - 120, (height / 2), 100, 20, new StringTextComponent("Price"));
        rentInput.setVisible(true);
        rentInput.setFocus(true);
        rentInput.setMaxLength(6);
        rentInput.setValue(String.valueOf(tile.getRent()));
        priceInput.setVisible(true);
        priceInput.setMaxLength(6);
        priceInput.setValue(String.valueOf(tile.getPrice()));
        children.add(rentInput);
        children.add(priceInput);
        buildingTypePanel.selection.type = tile.getBuildingType();
        buildingTypePanel.selection.id = tile.getBuildingType().id;
        buildingTypePanel.selection.string = tile.getBuildingType().name;
    }

    private void addBuildingTypeButtons() {
        int i = 0;
        for (BuildingType type : BuildingType.values()) {
            Button button = new Button(30, 60 + (i * 25), 100, 20, new StringTextComponent(type.name), butn -> {
                buildingTypePanel.selection.id = type.id;
                buildingTypePanel.selection.string = type.name;
                buildingTypePanel.selection.type = type;
                reactivateButtons(buildingTypes);
                butn.active = false;
            });
            buildingTypes.add(button);
            addButton(button);
            i++;
        }
    }

    private void UpdateServer() {
        try {
            tile.setRent(Float.parseFloat(rentInput.getValue()));
            tile.setPrice(Float.parseFloat(priceInput.getValue()));

        } catch (NumberFormatException e) {
            SimuKraft.LOGGER().debug("Incorrect input" + e);
            tile.setRent(0);
            tile.setPrice(0);
        }
        tile.setBuildingType(buildingTypePanel.selection.type);
        tile.setWidth(buildingWidth);
        tile.setHeight(buildingHeight);
        tile.setDepth(buildingDepth);

        SimUKraftPacketHandler.INSTANCE.sendToServer(new CustomDataSyncPacket(tile.getPrice(), tile.getRent(), tile.getBuildingType(), pos));
    }

    private void reactivateButtons(ArrayList<Button> buttons) {
        for (Button button : buttons) {
            button.active = true;
        }
    }

    private class ButtonScrollPanel extends ScrollPanel {

        private final ArrayList<Button> buttons;
        private final int maxButtonsVisible;
        private final Selection selection;
        private int contentOffset;
        private int buttonConstant = 30;

        public ButtonScrollPanel(Minecraft client, int width, int height, int top, int left, ArrayList<Button> buttons, int maxButtonsVisible, int contentOffset) {
            super(client, width, height, top, left);
            children.addAll(buttons);
            this.buttons = buttons;
            this.maxButtonsVisible = maxButtonsVisible;
            updateButtonPlacement();
            selection = new Selection(this);
            this.contentOffset = contentOffset;
        }

        private void updateButtonPlacement() {
            int i = 0;
            for (Button button : buttons) {
                int index = (((int) this.scrollDistance) / getScrollAmount());
                button.y = ((this.top + i * buttonConstant) - (index * buttonConstant));
                i++;
                button.visible = disableOuterButton(button);
            }
        }

        private boolean disableOuterButton(Button button) {
            return button.y >= this.top && button.y + button.getHeight() <= this.bottom;
        }

        @Override
        protected int getContentHeight() {
            return buttons.size() * buttonConstant + contentOffset;
        }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {

        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
            if (super.mouseScrolled(mouseX, mouseY, scroll)) {
                updateButtonPlacement();
                return true;

            }
            return false;
        }

        @Override
        protected int getScrollAmount() {
            return (int) Math.ceil((double) (this.buttons.size() * buttonConstant + contentOffset)/ (double) maxButtonsVisible);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                updateButtonPlacement();
                return true;
            }
            return false;
        }
    }

    private class Selection {
        private String string;
        private int id;
        private BuildingType type;

        Selection(ButtonScrollPanel panel) {
            string = panel.buttons.get(0).getMessage().getString();
            BuildingType type = tile.getBuildingType();
            this.type = type;
            if (type != null) {
                id = type.id;
            }
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public BuildingType getType() {
            return type;
        }
    }
}

