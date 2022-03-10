package com.student.fraudsters.detection.execution.task.algorith_execution;

import org.neo4j.driver.summary.SummaryCounters;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class CommunityDetectionAlgorithmTask extends ExecuteGdsAlgorithmTask {

    public CommunityDetectionAlgorithmTask(String algorithmDisplayName, String cypherTemplate, Map<String, String> substitutionValues) {
        super(GdsAlgorithmType.COMMUNITY_DETECTION, algorithmDisplayName, cypherTemplate, substitutionValues);
    }

    @Override
    protected void parseQueryOutput(Neo4jClient.RunnableSpec runnableSpec) {
        SummaryCounters counters = runnableSpec.run()
                .counters();
        int propertiesSet = counters.propertiesSet();
        setSuccessMessage(format("Successfully executed the algorithm '%s': propertiesSet=%d.", getAlgorithmDisplayName(), propertiesSet));
    }
}
