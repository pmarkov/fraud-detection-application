package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.task.core.BaseTask;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public class AddNodeLabelsTask extends BaseTask {

    private final String labelName;

    public AddNodeLabelsTask(String labelName, String cypherTemplate, Map<String, String> substitutionValues) {
        super(format("Add Node Label '%s'", labelName), cypherTemplate, substitutionValues);
        this.labelName = labelName;
    }

    @Override
    protected void executeInternal(Neo4jClient neo4jClient, String cypherQuery) {
        int labelsAdded = neo4jClient.query(cypherQuery)
                                     .run()
                                     .counters()
                                     .labelsAdded();
        setSuccessMessage(format("Successfully added %d labels '%s'.", labelsAdded, labelName));
    }
}
