package com.resimulators.simukraft.init;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.JobBuilder;
import com.resimulators.simukraft.common.jobs.JobMiner;
import com.resimulators.simukraft.common.jobs.core.IJob;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class ModJobs {
    public static final Map<Integer, Function<SimEntity, IJob>> JOB_LOOKUP = new ImmutableMap.Builder<Integer, Function<SimEntity, IJob>>()
            .put(1, JobBuilder::new)
            .put(2, JobMiner::new)


            .build();
}
