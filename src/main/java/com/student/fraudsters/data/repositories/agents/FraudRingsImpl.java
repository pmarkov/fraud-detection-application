package com.student.fraudsters.data.repositories.agents;

import com.student.fraudsters.data.entity.agents.FraudRingDto;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FraudRingsImpl implements FraudRings {

    public static final String GET_FIRST_PARTY_GROUP_IDS_BY_SIZE = "MATCH (c:Client) WHERE exists(c.firstPartyFraudGroup) " +
            "WITH c.firstPartyFraudGroup AS firstPartyGroupId, collect(c.id) AS clients " +
            "WITH *, size(clients) as groupSize " +
            "WHERE groupSize >= $minimumGroupSize " +
            "RETURN firstPartyGroupId, groupSize ORDER BY groupSize DESC";
    private final Neo4jClient neo4jClient;

    public FraudRingsImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public List<FraudRingDto> getFraudRingsOrderedBySize(int minimumSize) {
        return new ArrayList<>(neo4jClient.query(GET_FIRST_PARTY_GROUP_IDS_BY_SIZE)
                .bindAll(Map.of("minimumGroupSize", minimumSize))
                .fetchAs(FraudRingDto.class)
                .mappedBy((typeSystem, record) -> new FraudRingDto(record.get("firstPartyGroupId").asInt(), record.get("groupSize").asInt()))
                .all());
    }
}
