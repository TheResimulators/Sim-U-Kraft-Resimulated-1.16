package com.resimulators.simukraft.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class JobEditorStick extends Item {


    public JobEditorStick(Properties properties) {
        super(properties);
    }



    @Override
    public ActionResultType useOn(ItemUseContext p_195939_1_) {
        return ActionResultType.PASS;
    }
}
