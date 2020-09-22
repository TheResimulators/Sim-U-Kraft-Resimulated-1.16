package com.resimulators.simukraft.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class GuiCustomData extends Screen {
    private BlockPos pos;

    protected GuiCustomData(ITextComponent titleIn, BlockPos pos) {
        super(titleIn);
        this.pos = pos;
    }
}
