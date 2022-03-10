package com.student.fraudsters.data.entity.agents;

import com.student.fraudsters.data.entity.identity.Email;
import com.student.fraudsters.data.entity.identity.Phone;
import com.student.fraudsters.data.entity.identity.SSN;
import com.student.fraudsters.data.entity.transactions.Transaction;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;
import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node
public class Client extends BaseAgent {

    @Relationship(type = "HAS_SSN", direction = OUTGOING)
    private final SSN ssn;

    @Relationship(type = "HAS_PHONE", direction = OUTGOING)
    private final Phone phone;

    @Relationship(type = "HAS_EMAIL", direction = OUTGOING)
    private final Email email;

    @Relationship(type = "TO", direction = INCOMING)
    private final List<Transaction> incomingTransactions;

    @Relationship(type = "PERFORMED", direction = OUTGOING)
    private final List<Transaction> outgoingTransactions;

    @Relationship(type = "FIRST_TX", direction = OUTGOING)
    private final Transaction firstTransaction;

    @Relationship(type = "LAST_FX", direction = OUTGOING)
    private final Transaction lastTransaction;

    private final Integer firstPartyFraudGroup;

    private final Double secondPartyFraudScore;

    public Client(String id, String name, SSN ssn, Phone phone, Email email,
                  List<Transaction> incomingTransactions, List<Transaction> outgoingTransactions,
                  Transaction firstTransaction, Transaction lastTransaction,
                  Integer firstPartyFraudGroup, Double secondPartyFraudScore) {
        super(id, name);
        this.ssn = ssn;
        this.phone = phone;
        this.email = email;
        this.incomingTransactions = incomingTransactions;
        this.outgoingTransactions = outgoingTransactions;
        this.firstTransaction = firstTransaction;
        this.lastTransaction = lastTransaction;
        this.firstPartyFraudGroup = firstPartyFraudGroup;
        this.secondPartyFraudScore = secondPartyFraudScore;
    }

    public SSN getSsn() {
        return ssn;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public List<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }

    public List<Transaction> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public Transaction getFirstTransaction() {
        return firstTransaction;
    }

    public Transaction getLastTransaction() {
        return lastTransaction;
    }

    public Integer getFirstPartyFraudGroup() {
        return firstPartyFraudGroup;
    }

    public Double getSecondPartyFraudScore() {
        return secondPartyFraudScore;
    }
}
