package com.Resimulators.simukraft.common.block;

import net.minecraft.block.Block;

public class BlockLight extends BlockBase {



    public BlockLight(Properties properties, String name) {
        super(properties.lightValue(9), name);

    }
}
