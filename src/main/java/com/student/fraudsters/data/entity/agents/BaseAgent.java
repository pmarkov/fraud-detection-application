package com.student.fraudsters.data.entity.agents;

import org.springframework.data.neo4j.core.schema.Id;

public abstract class BaseAgent {

    @Id
    private final String id;
    private final String name;

    protected BaseAgent(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
