package com.Resimulators.simukraft.common.jobs.core;

import com.Resimulators.simukraft.common.jobs.JobBuilder;

public enum JobType {

    BUILDER (JobBuilder.class,"Bulder"),
    FARMER (IJob.class,"Farmer"),
    CATTLE_FARMER(IJob.class, "Cattle Farmer"),
    SHEEP_FARMER(IJob.class,"Sheep Farmer"),
    PIG_FARMER(IJob.class,"Pig Farmer"),
    GUARD(IJob.class,"Guard"), // could make subtypes for things like rogues, mages, archers, warriors
    MINER(IJob.class,"Miner"),
    BUTCHER(IJob.class,"Butcher"),
    GLASS_MAKER(IJob.class,"Glass Maker"),
    GROCER(IJob.class, "Grocer");

    private final Class<? extends IJob> job;
    private final String string;

    JobType(Class<?extends IJob> job,String string)
    {
        this.string = string;
        this.job= job;
    }

    public Class<? extends IJob> getJob(JobType type) {
        return type.job;
    }


    public String getString(JobType type) {
        return type.string;
    }


    public JobType getValueFromString(String string){
        for (JobType type:JobType.values()) {
            if (type.string == string){
                return type;
            }

        }
        return null;

    }
}
