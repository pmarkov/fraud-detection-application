package com.student.fraudsters.detection.execution.task.core;

import org.springframework.data.neo4j.core.Neo4jClient;

public interface Task {
    boolean execute(Neo4jClient neo4jClient);
    String getTaskDisplayName();
    String getInitialMessage();
    String getErrorMessage();
    String getSuccessMessage();
}
