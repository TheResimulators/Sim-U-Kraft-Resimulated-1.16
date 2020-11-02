package com.resimulators.simukraft.init;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.*;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class ModJobs {
    public static final Map<Integer, Function<SimEntity, IJob>> JOB_LOOKUP = new ImmutableMap.Builder<Integer, Function<SimEntity, IJob>>()
            .put(Profession.BUILDER.getId(), JobBuilder::new)
            .put(Profession.MINER.getId(), JobMiner::new)
            .put(Profession.GLASS_FACTORY.getId(), JobGlassFactory::new)
            .put(Profession.FARMER.getId(), JobFarmer::new)
            .put(Profession.ANIMAL_FARMER.getId(), JobAnimalFarmer::new)
            .build();

}
