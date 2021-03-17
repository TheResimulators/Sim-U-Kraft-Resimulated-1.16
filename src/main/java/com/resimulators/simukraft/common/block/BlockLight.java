package com.resimulators.simukraft.common.block;
import net.minecraft.block.AbstractBlock.Properties;


public class BlockLight extends BlockBase {
    public BlockLight(Properties properties) {
        super(properties.lightLevel((light) -> 9));

    }
}
