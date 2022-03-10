package com.student.fraudsters.data.entity.agents;

import com.student.fraudsters.data.entity.transactions.Transaction;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;

@Node
public class Merchant extends BaseAgent {

    @Relationship(type = "TO", direction = INCOMING)
    private final List<Transaction> incomingTransactions;

    public Merchant(String id, String name, List<Transaction> incomingTransactions) {
        super(id, name);
        this.incomingTransactions = incomingTransactions;
    }

    public List<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }
}
