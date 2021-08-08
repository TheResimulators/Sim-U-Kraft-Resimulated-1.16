package com.resimulators.simukraft.init;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.tileentity.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {
    private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Reference.MODID);
    public static final RegistryObject<TileEntityType<TileConstructor>> CONSTRUCTOR = REGISTRY.register("constructor", () -> TileEntityType.Builder.of(TileConstructor::new, ModBlocks.CONSTRUCTOR_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileMarker>> MARKER = REGISTRY.register("marker", () -> TileEntityType.Builder.of(TileMarker::new, ModBlocks.MARKER.get()).build(null));
    public static final RegistryObject<TileEntityType<TileMiner>> MINER = REGISTRY.register("miner", () -> TileEntityType.Builder.of(TileMiner::new, ModBlocks.MINE_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileFarmer>> FARMER = REGISTRY.register("farmer", () -> TileEntityType.Builder.of(TileFarmer::new, ModBlocks.FARM_BOX.get()).build(null));
    public static final RegistryObject<TileEntityType<TileGlassFactory>> GLASS_FACTORY = REGISTRY.register("glass_factory", () -> TileEntityType.Builder.of(TileGlassFactory::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileCustomData>> CUSTOM_DATA = REGISTRY.register("custom_data", () -> TileEntityType.Builder.of(TileCustomData::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileCowFarm>> COW_FARMER = REGISTRY.register("cow_farmer", () -> TileEntityType.Builder.of(TileCowFarm::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileSheepFarm>> SHEEP_FARMER = REGISTRY.register("sheep_farmer", () -> TileEntityType.Builder.of(TileSheepFarm::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileChickenFarmer>> CHICKEN_FARMER = REGISTRY.register("chicken_farmer", () -> TileEntityType.Builder.of(TileChickenFarmer::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TilePigFarmer>> PIG_FARMER = REGISTRY.register("pig_farmer", () -> TileEntityType.Builder.of(TilePigFarmer::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileBaker>> BAKER = REGISTRY.register("baker", () -> TileEntityType.Builder.of(TileBaker::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileFisher>> FISHER_MAN = REGISTRY.register("fisher_man", () -> TileEntityType.Builder.of(TileFisher::new, ModBlocks.CONTROL_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TileResidential>> RESIDENTIAL = REGISTRY.register("residential", () -> TileEntityType.Builder.of(TileResidential::new, ModBlocks.CONTROL_BLOCK.get()).build(null));

    public ModTileEntities() {
        REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}