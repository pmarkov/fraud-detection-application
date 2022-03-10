package com.student.fraudsters.detection.execution.task;


import com.student.fraudsters.detection.execution.task.algorith_execution.ExecuteGdsAlgorithmTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.neo4j.driver.Values.NULL;

@Testcontainers
@DataNeo4jTest
public class FirstPartyFraudDetectionTest {

    private final static String CLIENT1_ID = "123456";
    private final static String CLIENT2_ID = "234567";
    private final static String CLIENT3_ID = "654321";

    private static final String CREATE_CLIENTS_WITH_IDENTITIES = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "SET client3:Mule\n"
            + "CREATE (phone1:Phone {phoneNumber: '853-637-949'})\n"
            + "CREATE (phone2:Phone {phoneNumber: '123-456-789'})\n"
            + "CREATE (ssn1:SSN {ssn: '330-39-0677'})\n"
            + "CREATE (email1:Email {email: 'zoeyhorn330@yahoo.com'})\n"
            + "MERGE (client1)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client2)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client3)-[:HAS_PHONE]->(phone2)\n"
            + "MERGE (client1)-[:HAS_SSN]->(ssn1)\n"
            + "MERGE (client2)-[:HAS_SSN]->(ssn1)\n"
            + "MERGE (client1)-[:HAS_EMAIL]->(email1)\n"
            + "MERGE (client1)-[:SHARED_IDENTIFIERS { count: 2 }]-(client2)";

    private static final String CREATE_CLIENTS_WITH_IDENTITIES_AND_FRAUD_GROUP = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts', firstPartyFraudGroup: 1})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman', firstPartyFraudGroup: 1})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "SET client3:Mule\n"
            + "CREATE (phone1:Phone {phoneNumber: '853-637-949'})\n"
            + "CREATE (phone2:Phone {phoneNumber: '123-456-789'})\n"
            + "CREATE (ssn1:SSN {ssn: '330-39-0677'})\n"
            + "CREATE (email1:Email {email: 'zoeyhorn330@yahoo.com'})\n"
            + "MERGE (client1)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client2)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client3)-[:HAS_PHONE]->(phone2)\n"
            + "MERGE (client1)-[:HAS_SSN]->(ssn1)\n"
            + "MERGE (client2)-[:HAS_SSN]->(ssn1)\n"
            + "MERGE (client1)-[:HAS_EMAIL]->(email1)\n"
            + "MERGE (client1)-[:SHARED_IDENTIFIERS { count: 2 }]-(client2)";

    public static final String CREATE_CLIENTS_WITH_IDENTIFIERS_AND_SIMILAR_RELATIONSHIPS = "" +
            "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n" +
            "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n" +
            "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n" +
            "SET client3:Mule\n" +
            "CREATE (phone1:Phone {phoneNumber: '853-637-949'})\n" +
            "CREATE (phone2:Phone {phoneNumber: '123-456-789'})\n" +
            "CREATE (ssn1:SSN {ssn: '330-39-0677'})\n" +
            "CREATE (email1:Email {email: 'zoeyhorn330@yahoo.com'})\n" +
            "MERGE (client1)-[:HAS_PHONE]->(phone1)\n" +
            "MERGE (client2)-[:HAS_PHONE]->(phone1)\n" +
            "MERGE (client3)-[:HAS_PHONE]->(phone2)\n" +
            "MERGE (client1)-[:HAS_SSN]->(ssn1)\n" +
            "MERGE (client2)-[:HAS_SSN]->(ssn1)\n" +
            "MERGE (client1)-[:HAS_EMAIL]->(email1)\n" +
            "MERGE (client1)-[:SIMILAR_TO { similarityScore: 0.66 }]-(client2)";

    private static final String TEST_PROJECTION_NAME = "TestProjection";
    private static final String CREATE_TEST_GRAPH_PROJECTION_FOR_COMMUNITY_DETECTION = "" +
            "CALL gds.graph.create('"+ TEST_PROJECTION_NAME +"', 'Client',\n" +
            "{\n" +
            "    SHARED_IDENTIFIERS:{\n" +
            "        type: 'SHARED_IDENTIFIERS',\n" +
            "        properties: {\n" +
            "            count: {\n" +
            "                property: 'count'\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "   }\n" +
            ")";

    private static final String CREATE_TEST_GRAPH_PROJECTION_FOR_SIMILARITY = "" +
            "CALL gds.graph.create.cypher('" + TEST_PROJECTION_NAME + "',\n" +
            "'MATCH(c:Client)\n" +
            "    WHERE exists(c.firstPartyFraudGroup)\n" +
            "    RETURN id(c) AS id, labels(c) AS labels\n" +
            "UNION\n" +
            "MATCH(n)\n" +
            "    WHERE n:Email OR n:Phone OR n:SSN\n" +
            "    RETURN id(n) AS id,labels(n) AS labels',\n" +
            "'MATCH(c:Client)\n" +
            "-[:HAS_EMAIL|HAS_PHONE|HAS_SSN]->(ids)\n" +
            "WHERE exists(c.firstPartyFraudGroup)\n" +
            "RETURN id(c) AS source,id(ids) AS target')";

    private static final String DELETE_TRANSACTION = "MATCH (n) DETACH DELETE n";

    @Container
    private static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.2")
            .withPlugins(MountableFile.forClasspathResource("/plugins"))
            .withNeo4jConfig("dbms.security.procedures.unrestricted", "gds.*,apoc.*");

    @DynamicPropertySource
    static void setNeo4jContainer(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @Autowired
    Neo4jClient neo4jClient;

    @BeforeEach
    void setUp(@Autowired Driver driver) {
        TaskFactory.getDropGraphProjectionTask(TEST_PROJECTION_NAME).execute(neo4jClient);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_WITH_IDENTITIES));
        }
    }

    @Test
    void should_run_wcc_community_detection_algorithm() {
        createTestProjection(CREATE_TEST_GRAPH_PROJECTION_FOR_COMMUNITY_DETECTION);
        ExecuteGdsAlgorithmTask task = TaskFactory.getCommunityDetectionForClientClustersTask("wcc", TEST_PROJECTION_NAME);

        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage()
                       .matches("Successfully executed the algorithm 'Weakly Connected Components': propertiesSet=2\\."));
        assertEquals(Values.NULL, getClientNodeProperty(CLIENT3_ID, "firstPartyFraudGroup"));
        assertEquals(0, getClientNodeProperty(CLIENT1_ID, "firstPartyFraudGroup").asInt());
        assertEquals(0, getClientNodeProperty(CLIENT2_ID, "firstPartyFraudGroup").asInt());
    }

    @Test
    void should_run_node_similarity_algorithm(@Autowired Driver driver) {
        recreateNodes(driver, CREATE_CLIENTS_WITH_IDENTITIES_AND_FRAUD_GROUP);
        createTestProjection(CREATE_TEST_GRAPH_PROJECTION_FOR_SIMILARITY);
        ExecuteGdsAlgorithmTask task = TaskFactory.getSimilarityAlgorithmForClientsInFraudRingsTask("nodeSimilarity", TEST_PROJECTION_NAME);
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Successfully executed the algorithm 'Node Similarity': relationshipsWritten=2, " +
                "nodesCompared=2, createMillis=\\d+, computeMillis=\\d+, writeMillis=\\d+\\."));
    }

    @Test
    void should_run_pageRank_centrality_algorithm(@Autowired Driver driver) {
        recreateNodes(driver, CREATE_CLIENTS_WITH_IDENTIFIERS_AND_SIMILAR_RELATIONSHIPS);
        ExecuteGdsAlgorithmTask task = TaskFactory.getCentralityAlgorithmForSimilarClientsInFraudRingsTask("pageRank");
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Successfully executed the algorithm 'Page Rank': nodePropertiesWritten=3, " +
                "ranIterations=2, didConverge=true, createMillis=\\d+, computeMillis=\\d+, writeMillis=\\d+\\."));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT1_ID, "firstPartyFraudScore"));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT2_ID, "firstPartyFraudScore"));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT3_ID, "firstPartyFraudScore"));
    }

    private Value getClientNodeProperty(String clientId, String propertyName) {
        return neo4jClient.query("MATCH (c: Client {id: $clientId}) RETURN c")
                .bindAll(Map.of("clientId", clientId))
                .fetchAs(Value.class)
                .mappedBy((typeSystem, record) -> record.get(0).asNode().get(propertyName))
                .one()
                .orElse(NULL);
    }

    private void recreateNodes(Driver driver, String createCypherQuery) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(createCypherQuery));
        }
    }

    private void createTestProjection(String cypherQuery) {
        neo4jClient.query(cypherQuery).run();
    }
}
