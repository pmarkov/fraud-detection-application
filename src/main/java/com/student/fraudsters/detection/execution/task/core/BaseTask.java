package com.student.fraudsters.detection.execution.task.core;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import static java.lang.String.format;

public abstract class BaseTask implements Task {

    private String initialMessage;
    private String successMessage;
    private String errorMessage;

    private final String taskDisplayName;
    private final String cypherTemplate;
    private final Map<String, String> substitutionValues;

    public BaseTask(String taskDisplayName, String cypherTemplate, Map<String, String> substitutionValues) {
        this.taskDisplayName = taskDisplayName;
        this.cypherTemplate = cypherTemplate;
        this.substitutionValues = substitutionValues;
        setInitialMessage(format("Running Task '%s'.", taskDisplayName));
    }

    @Override
    public boolean execute (Neo4jClient neo4jClient) {
        String cypherQuery = substituteValues();
        System.out.println("[DEBUG] cypherQuery: " + cypherQuery);
        try {
            executeInternal(neo4jClient, cypherQuery);
            return true;
        } catch (Exception e) {
            setErrorMessage(format("Execution of '%s' failed with: %s", taskDisplayName, e.getMessage()));
            return false;
        }
    }

    private String substituteValues() {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(substitutionValues);
        return stringSubstitutor.replace(cypherTemplate);
    }

    protected abstract void executeInternal(Neo4jClient neo4jClient, String cypherQuery);

    @Override
    public String getInitialMessage() {
        return initialMessage;
    }

    protected void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    @Override
    public String getSuccessMessage() {
        return successMessage;
    }

    protected void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getTaskDisplayName() {
        return taskDisplayName;
    }
}
