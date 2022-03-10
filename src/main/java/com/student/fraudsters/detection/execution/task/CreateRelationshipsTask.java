package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.task.core.BaseTask;
import org.neo4j.driver.summary.SummaryCounters;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class CreateRelationshipsTask extends BaseTask {

    public CreateRelationshipsTask(String relationshipType, String cypherTemplate, Map<String, String> substitutionValues) {
        super(format("Create '%s' Relationships", relationshipType), cypherTemplate, substitutionValues);
    }

    @Override
    protected void executeInternal(Neo4jClient neo4jClient, String cypherQuery) {
        SummaryCounters counters = neo4jClient.query(cypherQuery).run().counters();
        int relationshipsCreated = counters.relationshipsCreated();
        int propertiesSet = counters.propertiesSet();
        setSuccessMessage(format("Successfully created %d " + ((relationshipsCreated == 1) ? "relationship" : "relationships")
                + " and set %d " + ((propertiesSet == 1) ? "property." : "properties."), relationshipsCreated, propertiesSet));
    }
}
