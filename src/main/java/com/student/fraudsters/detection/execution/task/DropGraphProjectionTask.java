package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.CypherConstants;
import com.student.fraudsters.detection.execution.task.core.BaseTask;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class DropGraphProjectionTask extends BaseTask {

    private final String graphProjection;

    public DropGraphProjectionTask(String graphProjection, String cypherTemplate, Map<String, String> substitutionValues) {
        super(format("Delete projection with name '%s'", graphProjection), cypherTemplate, substitutionValues);
        this.graphProjection = graphProjection;
    }

    @Override
    protected void executeInternal(Neo4jClient neo4jClient, String cypherQuery) {
        if (!graphProjectionExists(neo4jClient)) {
            setSuccessMessage(format("Graph projection '%s' is already deleted.", graphProjection));
            return;
        }
        neo4jClient.query(cypherQuery).run();
        setSuccessMessage(format("Successfully deleted graph projection '%s'.", graphProjection));
    }

    private boolean graphProjectionExists(Neo4jClient neo4jClient) {
        return neo4jClient.query(CypherConstants.EXISTS_GRAPH_PROJECTION_TEMPLATE)
                .bindAll(Map.of("projectionName", graphProjection))
                .fetchAs(Boolean.class)
                .mappedBy(((typeSystem, record) -> record.get("exists").asBoolean()))
                .one()
                .orElseThrow();
    }
}
