package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class BlockItemBase extends BlockItem {
    public BlockItemBase(Block block, Properties properties, String name) {
        super(block, properties);
        this.setRegistryName(Reference.MODID, name);
    }
}
