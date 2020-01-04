package com.Resimulators.simukraft.common.block;

import com.Resimulators.simukraft.Reference;
import net.minecraft.block.Block;

public class BlockBase extends Block {
    public BlockBase(Properties properties,String name) {
        super(properties);
        this.setRegistryName(Reference.MODID,name);
    }
}
