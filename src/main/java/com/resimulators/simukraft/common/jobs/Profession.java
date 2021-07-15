package com.resimulators.simukraft.common.jobs;


public enum Profession {

    UNEMPLOYED("unemployed", 0),
    BUILDER("builder", 1),
    MINER("miner", 2),
    ANIMAL_FARMER("animal farmer", 3),
    FARMER("farmer", 4),
    GLASS_FACTORY("glass maker", 5),
    FISHER_MAN("fisher_man", 6),
    BAKER("baker", 7);


    String name;
    int id;

    public static String getNameFromID(int id) {
        for (Profession job : Profession.values()) {
            if (job.id == id) {
                return job.name;
            }
        }
        return null;
    }

    public static int getIDFromName(String name) {
        for (Profession job : Profession.values()) {
            if (job.name.equals(name)) {
                return job.id;
            }
        }
        return 0;
    }

    Profession(String name, int i) {
        this.name = name;
        this.id = i;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
