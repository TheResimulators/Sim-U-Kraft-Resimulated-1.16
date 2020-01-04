package com.Resimulators.simukraft.common.item;

import com.Resimulators.simukraft.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class BlockItemBase extends BlockItem {
    public BlockItemBase(Block block, Properties properties, String name) {
        super(block,properties);
        this.setRegistryName(Reference.MODID,name);
    }
}
