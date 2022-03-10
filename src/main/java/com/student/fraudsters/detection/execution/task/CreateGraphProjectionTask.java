package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.task.core.BaseTask;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class CreateGraphProjectionTask extends BaseTask {

    private final String graphProjectionName;

    public CreateGraphProjectionTask(String graphProjectionName, String cypherTemplate, Map<String, String> substitutionValues) {
        super("Create Graph Projection - " + graphProjectionName, cypherTemplate, substitutionValues);
        this.graphProjectionName = graphProjectionName;
    }

    @Override
    protected void executeInternal(Neo4jClient neo4jClient, String cypherQuery) {
        Map<String, Object> result = neo4jClient.query(cypherQuery)
                .fetch()
                .one()
                .orElseThrow();
        String nodeCount = result.get("nodeCount").toString();
        String relationshipCount = result.get("relationshipCount").toString();
        String createMillis = result.get("createMillis").toString();
        setSuccessMessage(format("Successfully created '%s', nodeCount=%s, relationshipCount=%s, createMillis=%s.", graphProjectionName, nodeCount, relationshipCount, createMillis));
    }
}
