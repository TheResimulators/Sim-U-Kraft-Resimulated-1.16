package com.resimulators.simukraft.common.jobs;


public enum Profession {

    UNEMPLOYED("unemployed",Profession.nextID()),
    BUILDER("builder",Profession.nextID()),
    MINER("miner",Profession.nextID()),
    COW_FARMER("cow farmer",Profession.nextID()),
    SHEEP_FARMER("sheep farmer",Profession.nextID()),
    PIG_FARMER("cow farmer", Profession.nextID()),
    FARMER("farmer",Profession.nextID());

    Profession(String name, int i) {
        this.name = name;
        this.id = i;
    }
    String name;
    int id;
    private static int ID;

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

    private static int nextID(){
        return ID++;
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
