package com.resimulators.simukraft.init;

import com.resimulators.simukraft.SimuKraft;
import net.minecraft.world.GameRules;

public class ModGameRules {



    public static GameRules.RuleKey<GameRules.BooleanValue> Creative_Build_Mode_Rule;




    public static void setupGameRules()
    {
        Creative_Build_Mode_Rule = GameRules.register("SimUKraft:Creative-Build-Mode", GameRules.Category.MISC, SimuKraft.create(false));


    }
}
