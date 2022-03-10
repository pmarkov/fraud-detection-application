package com.student.fraudsters.detection.execution.task.algorith_execution;

import com.student.fraudsters.detection.execution.task.core.BaseTask;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public abstract class ExecuteGdsAlgorithmTask extends BaseTask {

    private final String algorithmDisplayName;

    public ExecuteGdsAlgorithmTask(GdsAlgorithmType algorithmType, String algorithmDisplayName, String cypherTemplate, Map<String, String> substitutionValues) {
        super(format("Execute GDS %s Algorithm '%s'", algorithmType.label, algorithmDisplayName), cypherTemplate, substitutionValues);
        this.algorithmDisplayName = algorithmDisplayName;
    }

    @Override
    protected void executeInternal(Neo4jClient neo4jClient, String cypherQuery) {
        parseQueryOutput(neo4jClient.query(cypherQuery));
    }

    protected abstract void parseQueryOutput(Neo4jClient.RunnableSpec runnableSpec);

    public String getAlgorithmDisplayName() {
        return algorithmDisplayName;
    }
}
