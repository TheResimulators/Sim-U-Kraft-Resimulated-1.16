package com.Resimulators.simukraft.datagen;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.init.ModBlocks;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
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

public class LootTableGen implements IDataProvider {
    public LootTableGen(DataGenerator dataGeneratorIn) {
        this.generator = dataGeneratorIn;
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected final Map<Block,LootTable.Builder> lootTables = new HashMap<>();
    private final DataGenerator generator;

    @Override
    public void act(DirectoryCache cache) throws IOException
    {
        Map<ResourceLocation,LootTable> tables = new HashMap<>();

        addTables();

        for(Map.Entry<Block,LootTable.Builder> entry : lootTables.entrySet())
        {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParameterSet(LootParameterSets.BLOCK).build());
        }

        tables.forEach((key, lootTable) -> {
            try
            {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), generator.getOutputFolder().resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getName() {
        return "Sim u kraft Block Loot Tables";
    }



        protected void addTables(){
            for (Block block:getKnownBlocks()){

                lootTables.put(block,registerDropSelfLootTable(block));
            }


        }
    protected final LootTable.Builder registerDropSelfLootTable(Block block)
    {
        return LootTable.builder()
                .addLootPool(LootPool.builder()
                        .rolls(ConstantRange.of(1))
                        .addEntry(ItemLootEntry.builder(block))
                        .acceptCondition(SurvivesExplosion.builder()));
    }


    protected Iterable<Block> getKnownBlocks(){
        return ForgeRegistries.BLOCKS.getValues()
                .stream()
                .filter((block) -> block.getRegistryName().getNamespace().equals(Reference.MODID))
                .collect(Collectors.toList());
    }

    }
