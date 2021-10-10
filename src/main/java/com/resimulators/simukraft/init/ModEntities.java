package com.resimulators.simukraft.init;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.client.render.EntitySimRender;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

//TODO: fabbe50: update to deferred registry !lowprio
@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    private static final List<EntityType> entities = Lists.newArrayList();
    private static final List<Item> spawmEggs = Lists.newArrayList();

    public static final EntityType<SimEntity> ENTITY_SIM = createEntity(SimEntity.class, SimEntity::new, EntityClassification.CREATURE, 0.6875f, 1.8f, 0x07b351, 0x614500);

    public static void init(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(ENTITY_SIM);
        SimuKraft.LOGGER().debug("Entities registered");
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        for (EntityType<?> entity : entities) {
            Preconditions.checkNotNull(entity.getRegistryName(), "registryName");
            event.getRegistry().register(entity);
        }
    }

    @SubscribeEvent
    public static void registerSpawnEggs(RegistryEvent.Register<Item> event) {
        for (Item spawnEgg : spawmEggs) {
            Preconditions.checkNotNull(spawnEgg.getRegistryName(), "registryName");
            event.getRegistry().register(spawnEgg);
        }
    }

    public static void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ENTITY_SIM, EntitySimRender::new);
    }

    public static void registerAttributes() {
        GlobalEntityTypeAttributes.put(ENTITY_SIM, SimEntity.createAttributes().build());
    }

    private static <T extends Entity> EntityType<T> createEntity(Class<T> entityClass, EntityType.IFactory<T> factory, EntityClassification classification, float width, float height, int eggPrimary, int eggSecondary) {
        ResourceLocation location = new ResourceLocation(Reference.MODID, classToString(entityClass));
        EntityType<T> entity = EntityType.Builder.of(factory, classification).sized(width, height).setTrackingRange(64).setUpdateInterval(1).build(location.toString());
        entity.setRegistryName(location);
        entities.add(entity);
        Item spawnEgg = new SpawnEggItem(entity, eggPrimary, eggSecondary, (new Item.Properties()).tab(ItemGroup.TAB_MISC));
        spawnEgg.setRegistryName(new ResourceLocation(Reference.MODID, classToString(entityClass) + "_spawn_egg"));
        spawmEggs.add(spawnEgg);
        return entity;
    }

    private static String classToString(Class<? extends Entity> entityClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName()).replace("_entity", "");
    }
}
