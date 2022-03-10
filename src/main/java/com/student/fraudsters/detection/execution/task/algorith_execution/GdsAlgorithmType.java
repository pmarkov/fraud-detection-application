package com.student.fraudsters.detection.execution.task.algorith_execution;

public enum GdsAlgorithmType {
    SIMILARITY("Similarity"),
    COMMUNITY_DETECTION("Community Detection"),
    CENTRALITY("Centrality");

    public final String label;

    GdsAlgorithmType(String label) {
        this.label = label;
    }
}
