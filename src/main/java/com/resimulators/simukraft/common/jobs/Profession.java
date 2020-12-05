package com.resimulators.simukraft.common.jobs;


public enum Profession {

    UNEMPLOYED("unemployed",0),
    BUILDER("builder",1),
    MINER("miner",2),
    ANIMAL_FARMER("animal farmer",3),
    FARMER("farmer",4),
    GLASS_FACTORY("glass maker", 5),
    Fisher("fisher",6);

    Profession(String name, int i) {
        this.name = name;
        this.id = i;
    }
    String name;
    int id;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static String getNameFromID(int id){
        for (Profession job:Profession.values() ){
            if (job.id == id){
                return job.name;
            }
        }
        return null;
    }

    public static int getIDFromName(String name){
        for (Profession job:Profession.values() ){
            if (job.name.equals(name)){
                return job.id;
            }
        }
        return 0;
    }

}
