package com.example.model;

public class Orchestrator {

    private static final Orchestrator orchestrator;

    static {
        // Do other things here 
        orchestrator = new Orchestrator();
    }

    public static Orchestrator getOrchestrator() {
        return orchestrator;
    }

}