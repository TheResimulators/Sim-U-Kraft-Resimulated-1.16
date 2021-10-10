package com.resimulators.simukraft.common.block;

public class BlockLight extends BlockBase {
    public BlockLight(Properties properties) {
        super(properties.lightLevel((light) -> 9));

    }
}
