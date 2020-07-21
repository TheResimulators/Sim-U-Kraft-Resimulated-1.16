package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.text.StringTextComponent;

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
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);
        if (loaded) {
            func_230480_a_(Build = new LargeButton(width / 2 - 55, height - 55, 110, 42, new StringTextComponent("Build"), (Build -> {
                super.hideAll();
                CustomBack.field_230694_p_ = true;
                state = State.SELECTBULDING;
            })));
            //Build.active=false;
            func_230480_a_(CustomBack = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Back"), (Back -> {
                super.Back.func_230930_b_();
                if (state == State.SELECTBULDING) {
                    state = State.MAIN;
                    showMainMenu();

                }
                if (state == State.BUILDINGINFO) {
                    state = State.SELECTBULDING;
                }

            }


            )));
            if (!isHired()) {
                Build.field_230693_o_ = false;
            }
            CustomBack.field_230694_p_ = false;
        }
    }

    public void loadBuildings(ArrayList<Structure> structures) {
        this.loaded = true;
        this.structures = structures;
        func_231160_c_(); //init
    }

    public void setStructures(ArrayList<Structure> structures) {
        this.structures = structures;

    }

    @Override
    public void func_230430_a_(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        func_230446_a_(stack);
        if (loaded) super.func_230430_a_(stack, p_render_1_, p_render_2_, p_render_3_);
        else {
            field_230712_o_.func_238421_b_(stack, "Loading", (float) field_230708_k_ / 2 - field_230712_o_.getStringWidth("Loading") / 2, (float) field_230709_l_ / 2, Color.WHITE.getRGB());
        }

    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
        Build.field_230694_p_ = true;
    }

    static class State extends BaseJobGui.State {
        private static final int SELECTBULDING = nextID();
        private static final int BUILDINGINFO = nextID();


    }

    private class LargeButton extends Button {
        final ResourceLocation LARGE_BUTTON = new ResourceLocation(Reference.MODID, "textures/gui/large_button.png");

        public LargeButton(int widthIn, int heightIn, int width, int height, ITextComponent text, IPressable onPress) {
            super(widthIn, heightIn, width, height, text, onPress);
        }

        @Override
        public void func_230431_b_(MatrixStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(LARGE_BUTTON);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.field_230695_q_);
            int i = this.func_230989_a_(this.func_230449_g_());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.func_238474_b_(stack, this.field_230690_l_, this.field_230691_m_, 0, 1 + i * 42, this.field_230688_j_ / 2, this.field_230689_k_);
            this.func_238474_b_(stack, this.field_230690_l_ + this.field_230688_j_ / 2, this.field_230691_m_, 200 - this.field_230688_j_ / 2, 1 + i * 42, this.field_230688_j_ / 2, this.field_230689_k_);
            this.func_230441_a_(stack, minecraft, p_renderButton_1_, p_renderButton_2_);
            int j = getFGColor();
            this.func_238471_a_(stack, fontrenderer, this.func_230458_i_().getString(), this.field_230690_l_ + this.field_230688_j_ / 2, this.field_230691_m_ + (this.field_230689_k_ - 8) / 2, j | MathHelper.ceil(this.field_230695_q_ * 255.0F) << 24);

        }


        @Override
        public int func_230989_a_(boolean hovered) {
            int i = 0;
            if (!this.field_230693_o_) {
                i = 1;
            } else if (hovered) {
                i = 2;
            }

            return i;
        }
    }
}
