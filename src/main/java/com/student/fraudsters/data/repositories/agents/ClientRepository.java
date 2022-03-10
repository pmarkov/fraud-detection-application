package com.student.fraudsters.data.repositories.agents;

import com.student.fraudsters.data.entity.agents.Client;
import com.student.fraudsters.data.entity.agents.ClientDto;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ClientRepository extends Repository<Client, String>, FraudRings {
    long count();

    @Query("MATCH (c:Client) WHERE c:Mule " +
            "RETURN count(c) AS count")
    long countMules();

    Client findClientById(String id);

    @Query("MATCH (c1:Client)-[:HAS_PHONE|HAS_SSN|HAS_EMAIL]->()<-[:HAS_PHONE|HAS_SSN|HAS_EMAIL]-(c2:Client) " +
            "WHERE c1.id <> c2.id " +
            "RETURN count(DISTINCT c1.id) AS count")
    long countClientsWithSharedIdentifiers();

    @Query("MATCH (c:Client) WHERE exists (c.firstPartyFraudGroup) " +
            "RETURN c ORDER BY c.firstPartyFraudGroup ASC")
    List<ClientDto> findClientsInFraudRings();

    List<ClientDto> findClientsByFirstPartyFraudGroup(int firstPartyFraudGroup);

    @Query("MATCH (c:Client) WHERE c:FirstPartyFraudster RETURN c")
    List<ClientDto> findFirstPartyFraudsters();

    @Query("MATCH (c:Client) WHERE c:SecondPartyFraudSuspect RETURN c")
    List<ClientDto> findSecondPartyFraudSuspects();

    @Query("MATCH (c:Client) WHERE exists(c.secondPartyFraudScore) " +
            "WITH percentileCont(c.secondPartyFraudScore, $percentileThreshold) AS secondPartyFraudThreshold " +
            "MATCH(c:Client) WHERE c.secondPartyFraudScore > secondPartyFraudThreshold " +
            "RETURN c")
    List<ClientDto> findSecondPartyFraudstersByFraudScore(double percentileThreshold);
}
