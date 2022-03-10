package com.student.fraudsters.detection.execution.task.algorith_execution;

public enum GdsAlgorithmTier {

    PRODUCTION("Production", "."),
    BETA("Beta", ".beta."),
    ALPHA("Alpha", ".alpha.");

    public final String label;
    public final String procedureInvocation;

    GdsAlgorithmTier(String label, String procedureInvocation) {
        this.label = label;
        this.procedureInvocation = procedureInvocation;
    }
}
