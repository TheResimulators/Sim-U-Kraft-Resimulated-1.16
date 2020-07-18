package com.resimulators.simukraft.init;

public class RegistryHandler {
    //Registry Handler is needed for new DeferredRegister system. Will eventually convert all registries to this method.
    public static void init() {
        new ModContainers();
    }
}
