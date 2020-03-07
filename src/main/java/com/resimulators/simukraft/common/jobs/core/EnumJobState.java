package com.resimulators.simukraft.common.jobs.core;

public enum EnumJobState {
        LOOKING_FOR_BLOCK,
        LOOKING_FOR_RESOURCES,
        WORKING,
        RETURNING,
        NOT_WORKING,
        GOING_TO_WORK,
        FORCE_STOP //use force stop for anything that should make the work stop work immediately


    }
