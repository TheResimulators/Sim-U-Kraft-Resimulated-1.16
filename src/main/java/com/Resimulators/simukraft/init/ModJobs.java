package com.Resimulators.simukraft.init;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.jobs.JobBuilder;
import com.Resimulators.simukraft.common.jobs.JobMiner;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class ModJobs {
    public static final Map<String, Function<EntitySim, IJob>> JOB_LOOKUP =new ImmutableMap.Builder<String, Function<EntitySim, IJob>>()
            .put("builder", JobBuilder::new)
            .put("miner", JobMiner::new)


            .build();
}
