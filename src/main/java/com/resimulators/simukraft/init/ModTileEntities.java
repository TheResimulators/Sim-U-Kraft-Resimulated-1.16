package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.tileentity.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ModTileEntities {
    private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Reference.MODID);

    public ModTileEntities() {
        REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<TileEntityType<TileConstructor>> CONSTRUCTOR = REGISTRY.register("constructor", () -> TileEntityType.Builder.create(TileConstructor::new, ModBlocks.CONSTRUCTOR_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileMarker>> MARKER = REGISTRY.register("marker", () -> TileEntityType.Builder.create(TileMarker::new, ModBlocks.MARKER.get()).build(null));
    public static final RegistryObject<TileEntityType<TileMiner>> MINER = REGISTRY.register("miner", () -> TileEntityType.Builder.create(TileMiner::new, ModBlocks.MINE_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileGlassFactory>> GLASS_FACTORY = REGISTRY.register("glass_factory", () -> TileEntityType.Builder.create(TileGlassFactory::new, ModBlocks.CONTROL_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileFarmer>> FARMER = REGISTRY.register("farmer", () -> TileEntityType.Builder.create(TileFarmer::new,ModBlocks.FARM_BOX.get()).build(null));
}