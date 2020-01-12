package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.SimUTab;
import com.Resimulators.simukraft.SimuKraft;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.event.RegistryEvent;

public class ModEntities {
    public static final EntityType<EntitySim> ENTITY_SIM = (EntityType<EntitySim>) EntityType.Builder.create(EntitySim::new, EntityClassification.AMBIENT).build(Reference.MODID + ":entity_sim").setRegistryName("entity_sim");


    public static void init(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(ENTITY_SIM);
        SimuKraft.LOGGER().debug("Entities registered");


    }


    static Item registerEntitySpawnegg(EntityType<EntitySim> entityType, int color1, int color2, String name) {
        SpawnEggItem item = new SpawnEggItem(entityType, color1, color2, new Item.Properties().group(SimUTab.tab));
        item.setRegistryName(Reference.MODID, name);
        return item;
    }
}
