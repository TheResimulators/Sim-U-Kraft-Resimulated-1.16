package com.resimulators.simukraft.common.jobs.core;

public enum Activity {
    WORKING("Working", 0),
    NOT_WORKING("Not Working", 1), // top be replaced with everything else the sims will do
    GOING_TO_WORK("Going to work", 2),
    TRAVELING("Traveling", 3),
    WANDERING("Wandering", 4),
    IDLING("Idling", 5),
    FORCE_STOP("Forced stop", 6); //use force stop for anything that should make the worker stop work/stop anything immediately

    public String name;
    public int id;

    public static Activity getActivityById(int id) {
        for (Activity activity : Activity.values()) {
            if (activity.id == id) {
                return activity;

            }
        }
        return Activity.IDLING;
    }


    Activity(String string, int id) {
        this.name = string;
        this.id = id;

    }
}
