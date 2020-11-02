package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.common.jobs.Profession;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;

public class GuiAnimal extends GuiBaseJob{


    public GuiAnimal(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, int id) {
        super(component, ids, pos, id, Profession.ANIMAL_FARMER.getId());
    }
}
