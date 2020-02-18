package com.Resimulators.simukraft.datagen;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.init.ModBlocks;
import com.Resimulators.simukraft.init.ModEntities;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.*;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider implements IDataProvider {
    public LootTableGen(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);

    }

    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTables = ImmutableList.of(
            Pair.of(ModLootTables::new, LootParameterSets.BLOCK),
            Pair.of(ModEntityLootTables::new,LootParameterSets.ENTITY)

    );


    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables(){
        return lootTables;
    }
    @Override
    public String getName() {
        return "Sim u kraft Block Loot Tables";
    }



    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((p_218436_2_, p_218436_3_) -> {
            LootTableManager.func_227508_a_(validationtracker, p_218436_2_, p_218436_3_);
        });
    }


    private static class ModLootTables extends BlockLootTables {

        @Override
        protected void addTables(){
            for (Block block : getKnownBlocks()) {
                super.registerDropSelfLootTable(block);
            }

        }
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ForgeRegistries.BLOCKS.getValues()
                    .stream()
                    .filter((block) -> block.getRegistryName().getNamespace().equals(Reference.MODID))
                    .collect(Collectors.toList());
        }
    }

    private static class ModEntityLootTables extends EntityLootTables{


        @Override
        protected void addTables(){
           super.registerLootTable(ModEntities.ENTITY_SIM,LootTable.builder());

        }


        @Override
        protected Iterable<EntityType<?>> getKnownEntities(){
            return ForgeRegistries.ENTITIES.getValues()
                    .stream()
                    .filter((entityType) -> entityType.getRegistryName().getNamespace().equals(Reference.MODID))
                    .collect(Collectors.toList());
        }
    }
}
