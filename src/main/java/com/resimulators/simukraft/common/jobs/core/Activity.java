package com.resimulators.simukraft.common.jobs.core;

public enum Activity {
        WORKING("Working"),
        NOT_WORKING("Not Working"), // top be replaced with everything else the sims will do
        GOING_TO_WORK("Going to work"),
        TRAVELING("Traveling"),
        WANDERING("Wandering"),
        IDLING("Idling"),
        FORCE_STOP("Forced stop"); //use force stop for anything that should make the worker stop work/stop anything immediately



        public String name;
        Activity(String string){
            this.name = string;

        }
    }
