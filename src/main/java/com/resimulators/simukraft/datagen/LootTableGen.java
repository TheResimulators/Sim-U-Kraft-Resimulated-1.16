package com.resimulators.simukraft.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.init.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider implements IDataProvider {
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTables = ImmutableList.of(
            Pair.of(ModLootTables::new, LootParameterSets.BLOCK),
            Pair.of(ModEntityLootTables::new, LootParameterSets.ENTITY)

    );

    public LootTableGen(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);

    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return lootTables;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((p_218436_2_, p_218436_3_) -> {
            LootTableManager.validate(validationtracker, p_218436_2_, p_218436_3_);
        });
    }

    @Override
    public String getName() {
        return "Sim u kraft Block Loot Tables";
    }

    private static class ModLootTables extends BlockLootTables {

        @Override
        protected void addTables() {
            for (Block block : getKnownBlocks()) {
                super.dropSelf(block);
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

    private static class ModEntityLootTables extends EntityLootTables {


        @Override
        protected void addTables() {
            super.add(ModEntities.ENTITY_SIM, LootTable.lootTable());

        }


        @Override
        protected Iterable<EntityType<?>> getKnownEntities() {
            return ForgeRegistries.ENTITIES.getValues()
                    .stream()
                    .filter((entityType) -> entityType.getRegistryName().getNamespace().equals(Reference.MODID))
                    .collect(Collectors.toList());
        }
    }
}
