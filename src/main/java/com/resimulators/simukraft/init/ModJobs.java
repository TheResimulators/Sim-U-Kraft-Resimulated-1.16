package com.resimulators.simukraft.init;

import com.google.common.collect.ImmutableMap;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.common.jobs.core.IReworkedJob;
import com.resimulators.simukraft.common.jobs.reworked.JobFarmer;

import java.util.Map;
import java.util.function.Function;

public class ModJobs {
    public static final Map<Integer, Function<SimEntity, IReworkedJob>> JOB_LOOKUP = new ImmutableMap.Builder<Integer, Function<SimEntity, IReworkedJob>>()
            .put(Profession.FARMER.getId(), JobFarmer::new)
            .build();

}
