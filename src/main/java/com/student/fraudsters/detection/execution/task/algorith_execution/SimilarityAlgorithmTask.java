package com.student.fraudsters.detection.execution.task.algorith_execution;

import org.neo4j.driver.Record;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class SimilarityAlgorithmTask extends ExecuteGdsAlgorithmTask {

    public SimilarityAlgorithmTask(String algorithmDisplayName, String cypherTemplate, Map<String, String> substitutionValues) {
        super(GdsAlgorithmType.SIMILARITY, algorithmDisplayName, cypherTemplate, substitutionValues);
    }

    @Override
    protected void parseQueryOutput(Neo4jClient.RunnableSpec runnableSpec) {
        Record resultRecord = runnableSpec.fetchAs(Record.class)
                                          .mappedBy((typeSystem, record) -> record)
                                          .one()
                                          .orElseThrow();
        int relationshipsWritten = resultRecord.get("relationshipsWritten").asInt();
        int nodesCompared = resultRecord.get("nodesCompared").asInt();
        int createMillis = resultRecord.get("createMillis").asInt();
        int computeMillis = resultRecord.get("computeMillis").asInt();
        int writeMillis = resultRecord.get("writeMillis").asInt();
        setSuccessMessage(format("Successfully executed the algorithm '%s': relationshipsWritten=%d, " +
                "nodesCompared=%d, createMillis=%d, computeMillis=%d, writeMillis=%d.",
                getAlgorithmDisplayName(), relationshipsWritten, nodesCompared, createMillis, computeMillis, writeMillis));
    }
}
