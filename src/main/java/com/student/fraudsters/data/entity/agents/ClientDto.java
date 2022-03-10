package com.student.fraudsters.data.entity.agents;

import com.student.fraudsters.data.entity.identity.Email;
import com.student.fraudsters.data.entity.identity.Phone;
import com.student.fraudsters.data.entity.identity.SSN;
import org.springframework.data.neo4j.core.schema.Relationship;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

public class ClientDto extends BaseAgent {

    @Relationship(type = "HAS_SSN", direction = OUTGOING)
    private final SSN ssn;

    @Relationship(type = "HAS_PHONE", direction = OUTGOING)
    private final Phone phone;

    @Relationship(type = "HAS_EMAIL", direction = OUTGOING)
    private final Email email;

    private final Integer firstPartyFraudGroup;
    private final Double secondPartyFraudScore;

    public ClientDto(String id, String name, SSN ssn, Phone phone, Email email, Integer firstPartyFraudGroup, Double secondPartyFraudScore) {
        super(id, name);
        this.ssn = ssn;
        this.phone = phone;
        this.email = email;
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

    public Integer getFirstPartyFraudGroup() {
        return firstPartyFraudGroup;
    }

    public Double getSecondPartyFraudScore() {
        return secondPartyFraudScore;
    }
}
