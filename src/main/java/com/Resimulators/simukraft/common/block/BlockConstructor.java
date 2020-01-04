package com.Resimulators.simukraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class BlockConstructor extends BlockBase {
    public BlockConstructor(final Properties properties,String name) {
        super(properties,name);

    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }
}
