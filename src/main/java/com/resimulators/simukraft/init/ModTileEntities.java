package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.tileentity.TileMiner;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.ArrayList;
import java.util.List;

public class ModTileEntities {
    private static List<TileEntityType> REGISTRY = new ArrayList<>();

    public static final TileEntityType<TileConstructor> CONSTRUCTOR = register(TileEntityType.Builder.create(TileConstructor::new, ModBlocks.CONSTRUCTOR_BOX), "constructor");
    public static final TileEntityType<TileMarker> MARKER = register(TileEntityType.Builder.create(TileMarker::new, ModBlocks.MARKER), "marker");
    public static final TileEntityType<TileMiner> MINER = register(TileEntityType.Builder.create(TileMiner::new, ModBlocks.MINE_BOX), "miner");
  
    private static <T extends TileEntity> TileEntityType<T> register(TileEntityType.Builder<T> builder, String name) {
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(Reference.MODID, name);
        REGISTRY.add(type);
        return type;
    }

    public static List<TileEntityType> getRegistry() {
        return REGISTRY;
    }
}