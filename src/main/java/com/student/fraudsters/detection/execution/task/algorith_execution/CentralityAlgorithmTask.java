package com.student.fraudsters.detection.execution.task.algorith_execution;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.summary.SummaryCounters;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class CentralityAlgorithmTask extends ExecuteGdsAlgorithmTask {

    private final boolean writeExecution;

    public CentralityAlgorithmTask(String algorithmDisplayName, boolean writeExecution, String cypherTemplate, Map<String, String> substitutionValues) {
        super(GdsAlgorithmType.CENTRALITY, algorithmDisplayName, cypherTemplate, substitutionValues);
        this.writeExecution = writeExecution;
    }

    @Override
    protected void parseQueryOutput(Neo4jClient.RunnableSpec runnableSpec) {
        System.out.println("[DEBUG] in parseQueryOutput writeExecution=" + writeExecution);
        if (writeExecution) {
            try {
                Record resultRecord = runnableSpec.fetchAs(Record.class)
                        .mappedBy((typeSystem, record) -> record)
                        .one()
                        .orElseThrow();
                setSuccessMessageForWriteModeExecution(resultRecord);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            setSuccessMessageForNonWriteModeExecution(runnableSpec.run().counters());
        }
    }

    private void setSuccessMessageForWriteModeExecution(Record resultRecord) {
        System.out.println("Converting nodePropertiesWritten");
        int nodePropertiesWritten = getIntegerValue(resultRecord.get("nodePropertiesWritten"));
        System.out.println("Converting ranIterations");
        int ranIterations = getIntegerValue(resultRecord.get("ranIterations"));
        System.out.println("Converting didConverge");
        boolean didConverge = getBooleanValue(resultRecord.get("didConverge"));
        System.out.println("Converting createMillis");
        int createMillis = getIntegerValue(resultRecord.get("createMillis"));
        System.out.println("Converting computeMillis");
        int computeMillis = getIntegerValue(resultRecord.get("computeMillis"));
        System.out.println("Converting writeMillis");
        int writeMillis = getIntegerValue(resultRecord.get("writeMillis"));
        setSuccessMessage(format("Successfully executed the algorithm '%s': nodePropertiesWritten=%d, " +
                        "ranIterations=%d, didConverge=%b, createMillis=%d, computeMillis=%d, writeMillis=%d.",
                getAlgorithmDisplayName(), nodePropertiesWritten, ranIterations, didConverge, createMillis, computeMillis, writeMillis));
    }

    private void setSuccessMessageForNonWriteModeExecution(SummaryCounters counters) {
        int propertiesSet = counters.propertiesSet();
        setSuccessMessage(format("Successfully executed the algorithm '%s': propertiesSet=%d.", getAlgorithmDisplayName(), propertiesSet));
    }

    private int getIntegerValue(Value value) {
        System.out.println("[DEBUG] in getIntegerValue");
//        if (value.isNull() || value.isEmpty()) {
//            System.out.println("[DEBUG] is null or empty");
//            return 0;
//        }
//        System.out.println("[DEBUG] is not null or empty");
        return value.asInt(0);
    }

    private boolean getBooleanValue(Value value) {
        System.out.println("[DEBUG] in getBooleanValue");
//        if (value.isNull() || value.isEmpty()) {
//            System.out.println("[DEBUG] is null or empty");
//            return false;
//        }
//        System.out.println("[DEBUG] is not null or empty");
        return value.asBoolean(false);
    }
}
