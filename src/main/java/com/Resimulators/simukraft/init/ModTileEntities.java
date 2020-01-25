package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.tileentity.TileConstructor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.ArrayList;
import java.util.List;

public class ModTileEntities {
    private static List<TileEntityType> REGISTRY = new ArrayList<>();

    public static final TileEntityType<TileConstructor> CONSTRUCTOR = register(TileEntityType.Builder.create(TileConstructor::new, ModBlocks.CONSTRUCTOR_BOX), "constructor");

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