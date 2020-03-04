package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.common.world.Structure;
import com.resimulators.simukraft.Reference;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

import java.awt.*;

import java.util.ArrayList;

public class BuilderGui extends BaseJobGui {
    private Button Build;
    private Button CustomBack;

    private boolean loaded = false;
    private ArrayList<Structure> structures;
    public BuilderGui(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, @Nullable int id) {
        super(component, ids, pos, id);
        this.job = "builder";
        loaded = true;// need to test
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
      if (loaded) {
            addButton(Build = new LargeButton(width / 2 - 55, height - 55, 110, 42, "Build", (Build -> {
                super.hideAll();
                CustomBack.visible = true;
                state = State.SELECTBULDING;
            })));
            //Build.active=false;
            addButton(CustomBack = new Button(width - 120, height - 30, 110, 20, "Back", (Back -> {
                super.Back.onPress();
                if (state == State.SELECTBULDING) {
                    state = State.MAIN;
                    showMainMenu();

                }
                if (state == State.BUILDINGINFO) {
                    state = State.SELECTBULDING;
                }

            }


            )));
        }
    }

    public void loadBuildings(ArrayList<Structure> structures) {
        this.loaded = true;
        this.structures = structures;
        init();
    }

    public void setStructures(ArrayList<Structure> structures) {
        this.structures = structures;

    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        if (loaded) super.render(p_render_1_, p_render_2_, p_render_3_);
        else {
            font.drawString("Loading", (float) width / 2 - font.getStringWidth("Loading")/2, (float) height / 2, Color.WHITE.getRGB());
        }

    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
        Build.visible = true;
    }

    static class State extends BaseJobGui.State {
        private static final int SELECTBULDING = nextID();
        private static final int BUILDINGINFO = nextID();


    }

    private class LargeButton extends Button {
        final ResourceLocation LARGE_BUTTON = new ResourceLocation(Reference.MODID, "textures/gui/large_button.png");


        public LargeButton(int widthIn, int heightIn, int width, int height, String text, IPressable onPress) {
            super(widthIn, heightIn, width, height, text, onPress);

        }



        @Override
        public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_){

            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(LARGE_BUTTON);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.blit(this.x, this.y, 0, 1 + i * 42, this.width / 2, this.height);
            this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 1 + i * 42, this.width / 2, this.height);
            this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
            int j = getFGColor();
            this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

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

    }
}
