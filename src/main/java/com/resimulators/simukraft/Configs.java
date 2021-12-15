package com.resimulators.simukraft;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class Configs {

    private final General general;
    private final Sims sims;
    private final Names names;
    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private final ForgeConfigSpec spec;

    public Configs() {
        general = new General();
        sims = new Sims();
        names = new Names();

        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    public General getGeneral() {
        return general;
    }

    public Sims getSims() {
        return sims;
    }

    public Names getNames() {
        return names;
    }

    public class General {
        //Put config variables here:


        public General() {
            //Creating the configuration category for each side.
            builder.comment("General settings").push("general");

            //Here's where the configuration options go:
            //WARNING: Don't use a '.'(period) in the "define"-method call. This completely BREAKS the config file.


            //Finishes off the configuration category.
            builder.pop();
        }
    }

    public class Sims {
        //Put config variables here:
        public final ForgeConfigSpec.IntValue specialSpawnChance;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> specialSimNames;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> specialSimGenders;
        public final ForgeConfigSpec.BooleanValue coloredNames;
        public final ForgeConfigSpec.DoubleValue teleportDistance;

        public Sims() {
            //Creating the configuration category for each side.
            builder.comment("Sims settings").push("sims");

            //Here's where the configuration options go:
            specialSpawnChance = builder.comment("How big is the chance that a special Sim spawns (1 in n)")
                    .defineInRange("Spawn Chance", 20, 1, Integer.MAX_VALUE);
            specialSimNames = builder.comment("Player usernames that will be assigned to the Sims.", "Skins of these players will also render on the Sims.")
                    .defineList("Special Names", ImmutableList.of("General5001", "Div4Wom4n", "fabbe50", "zakando", "Ellisenator", "jakegalen", "brodydavid1126", "Maiyr_Cordeth", "Korath", "SunCrAzy"), obj -> obj instanceof String);
            specialSimGenders = builder.comment("Names of skins that use the model with thin arms.")
                    .defineList("Thin model", ImmutableList.of("Div4Wom4n", "fabbe50"), obj -> obj instanceof String);
            coloredNames = builder.comment("Should you be able to color the Sim's names?")
                    .define("Colored Names", true);
            teleportDistance = builder.comment("Distance that a sim has to be for it to teleport to its destination").defineInRange("Distance",10d,1d,Double.MAX_VALUE);
            //Finishes off the configuration category.
            builder.pop();
        }
    }

    public class Names {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> femaleNames;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> maleNames;

        public Names() {
            builder.comment("Name settings").push("names");

            femaleNames = builder.comment("This holds all female names that gets applied to the Sims")
                    .defineList("Female Names", ImmutableList.of(
                            "Yen",
                            "Willette",
                            "Mindi",
                            "Krystle",
                            "Natalie",
                            "Amanda",
                            "Colene",
                            "Santana",
                            "Darlene",
                            "Nana",
                            "Yuri",
                            "Chia",
                            "Lady",
                            "Penney",
                            "Zita",
                            "Goldie",
                            "Diedra",
                            "Dawne",
                            "Emma",
                            "Elena",
                            "Phylis",
                            "Celestina",
                            "Mira",
                            "Sadye",
                            "Meda",
                            "Rachael",
                            "Donnette",
                            "Katrice",
                            "Denae",
                            "Afton",
                            "Charlotte",
                            "Bethany",
                            "Junko",
                            "Bonita",
                            "Sandra",
                            "Barbara",
                            "Enid",
                            "Emelina",
                            "Nanette",
                            "Sarai",
                            "Thi",
                            "Shannan",
                            "Wilma",
                            "Raguel",
                            "Ludie",
                            "Louisa",
                            "Lourdes",
                            "Cristen",
                            "Bess"),
                            obj -> obj instanceof String);
            maleNames = builder.comment("This holds all male names that gets applied to the Sims")
                    .defineList("Male Names", ImmutableList.of(
                            "Julian",
                            "Derick",
                            "Ronnie",
                            "Jeremy",
                            "Carson",
                            "Kim",
                            "Hank",
                            "Jospeh",
                            "Taylor",
                            "Marshall",
                            "Cordell",
                            "Andreas",
                            "Chase",
                            "Timothy",
                            "Lanny",
                            "Craig",
                            "William",
                            "Karl",
                            "Manual",
                            "Cletus",
                            "Agustin",
                            "Chad",
                            "Mauro",
                            "Thurman",
                            "Jeromy",
                            "Kareem",
                            "Jerome",
                            "Rudolph",
                            "Theodore",
                            "Jamel",
                            "Porter",
                            "Domingo",
                            "Thad",
                            "Elvin",
                            "Napoleon",
                            "Oswaldo",
                            "Randolph",
                            "Johnnie",
                            "Jeff",
                            "Grover",
                            "Noe",
                            "Aubrey",
                            "Corey",
                            "Romeo",
                            "Garland",
                            "Silas",
                            "Pedro",
                            "Mario",
                            "Isreal"
                            ),
                            obj -> obj instanceof String);

            builder.pop();
        }
    }
}
