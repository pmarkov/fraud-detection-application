package com.student.fraudsters.data.entity.transactions;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;


@Node(primaryLabel = "Transaction")
public class Transaction {

    @Id
    private final String id;
    private final int step;
    private final int globalStep;
    private final boolean fraud;
    private final double amount;
    private final int ts;

    public Transaction(String id, int step, int globalStep, boolean fraud, double amount, int ts) {
        this.id = id;
        this.step = step;
        this.globalStep = globalStep;
        this.fraud = fraud;
        this.amount = amount;
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public int getStep() {
        return step;
    }

    public int getGlobalStep() {
        return globalStep;
    }

    public boolean isFraud() {
        return fraud;
    }

    public double getAmount() {
        return amount;
    }

    public int getTs() {
        return ts;
    }
}
