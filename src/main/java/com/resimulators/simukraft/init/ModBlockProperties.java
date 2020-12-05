package com.resimulators.simukraft.init;

import com.resimulators.simukraft.common.enums.BuildingType;
import net.minecraft.state.IntegerProperty;

public class ModBlockProperties {

    public static final IntegerProperty TYPE = IntegerProperty.create("type", 1, BuildingType.getMaxValueId() + 1);
}
