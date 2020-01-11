package com.Resimulators.simukraft;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

import static net.minecraftforge.fml.loading.LogMarkers.CORE;

@Mod.EventBusSubscriber
public class Configs {
    private static final String CATEGORY_GENERAL = "general";

    private static final String LANG_KEY_ROOT = "config." + Reference.MODID;
    private static final String LANG_KEY_GENERAL = LANG_KEY_ROOT + "." + CATEGORY_GENERAL;
    private static final String LANG_KEY_SIMS = LANG_KEY_ROOT + ".sims";

    private static final Builder COMMON_BUILDER = new Builder();
    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();
    public static final CategorySims SIMS = new CategorySims();

    public static final class CategoryGeneral {
        //Put config variables here:


        private CategoryGeneral() {
            //Creating the configuration category for each side.
            COMMON_BUILDER.comment("General settings").push("general");
            SERVER_BUILDER.comment("General settings").push("general");
            CLIENT_BUILDER.comment("General settings").push("general");

            //Here's where the configuration options go:


            //Finishes off the configuration category.
            CLIENT_BUILDER.pop();
            COMMON_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    public static final class CategorySims {
        //Put config variables here:
        public final IntValue specialSpawnChance;
        public final ConfigValue<List<? extends String>> specialSimNames;
        public final ConfigValue<List<? extends String>> specialSimGenders;

        private CategorySims() {
            //Creating the configuration category for each side.
            SERVER_BUILDER.comment("Sims settings").push("sims");
            COMMON_BUILDER.comment("Sims settings").push("sims");
            CLIENT_BUILDER.comment("Sims settings").push("sims");

            //Here's where the configuration options go:
            specialSpawnChance = COMMON_BUILDER.comment("How big is the chance that a special Sim spawns (1 in n)")
                    .translation(LANG_KEY_SIMS + ".specialspawnchance")
                    .defineInRange("Spawn Chance", 1, 1, Integer.MAX_VALUE);
            specialSimNames = COMMON_BUILDER.comment("Player usernames that will be assigned to the Sims.", "Skins of these players will also render on the Sims.")
                    .translation(LANG_KEY_SIMS + ".simnames")
                    .defineList("Special Names", ImmutableList.of("General5001", "Div4Wom4n", "fabbe50", "zakando", "Ellisenator", "jakegalen", "brodydavid1126", "Maiyr_Cordeth", "Korath"), obj -> obj instanceof String);
            specialSimGenders = COMMON_BUILDER.comment("Names of skins that use the model with thin arms.")
                    .translation(LANG_KEY_SIMS + ".simgenders")
                    .defineList("Thin model", ImmutableList.of("Div4Wom4n", "fabbe50"), obj -> obj instanceof String);

            //Finishes off the configuration category.
            CLIENT_BUILDER.pop();
            COMMON_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    public static final ForgeConfigSpec COMMON_CONFIG = COMMON_BUILDER.build();
    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
    private static boolean serverCfgLoaded = false;

    private static void loadServerConfig() {
        serverCfgLoaded = true;
    }

    public static void onLoad(final ModConfig.Loading event) {
        if (event.getConfig().getSpec() == Configs.SERVER_CONFIG)
            loadServerConfig();
        SimuKraft.LOGGER().debug("Loaded {} config file {}", Reference.MODID, event.getConfig().getFileName());
    }

    public static void onFileChange(final ModConfig.ConfigReloading event) {
        SimuKraft.LOGGER().fatal(CORE, "{} config just got changed on the file system!", Reference.MODID);
    }

    public static boolean isServerConfigLoaded() {
        return serverCfgLoaded;
    }
}
