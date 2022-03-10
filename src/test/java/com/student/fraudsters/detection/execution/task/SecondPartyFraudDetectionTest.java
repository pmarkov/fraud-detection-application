package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.task.core.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
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
public class SecondPartyFraudDetectionTest {

    private final static String CLIENT1_ID = "123456";
    private final static String CLIENT2_ID = "234567";
    private final static String CLIENT3_ID = "654321";
    private final static String CLIENT4_ID = "567890";
    private final static int TRANSFER1_AMOUNT = 15000;
    private final static int TRANSFER2_AMOUNT = 15000;
    private final static int TRANSFER3_AMOUNT = 10000;

    private static final String CREATE_CLIENTS_WITH_TRANSACTIONS = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "CREATE (client4:Client {id: '" + CLIENT4_ID + "', name: 'Bat Milko'})\n"
            + "SET client1:FirstPartyFraudster\n"
            + "SET client3:Mule\n"
            + "CREATE (transfer1:Transaction {id: 'tx-1', globalStep: 1, step: 1, ts: 1, fraud: false, amount: " + TRANSFER1_AMOUNT + "})\n"
            + "SET transfer1:Transfer\n"
            + "CREATE (transfer2:Transaction {id: 'tx-2', globalStep: 803862, step: 719, ts: 1, fraud: false, amount:" + TRANSFER2_AMOUNT + "})\n"
            + "SET transfer2:Transfer\n"
            + "CREATE (transfer3:Transaction {id: 'tx-11', globalStep: 803863, step: 720, ts: 1, fraud: true, amount: " + TRANSFER3_AMOUNT +  "})\n"
            + "SET transfer3:Transfer\n"
            + "MERGE (client1)-[:PERFORMED]->(transfer1)-[:TO]->(client4)"
            + "MERGE (client4)-[:PERFORMED]->(transfer2)-[:TO]->(client1)"
            + "MERGE (client4)-[:PERFORMED]->(transfer3)-[:TO]->(client1)";

    private static final String CREATE_CLIENTS_WITH_TRANSACTIONS_AND_TRANSFER_TO_RELATIONSHIPS = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "CREATE (client4:Client {id: '" + CLIENT4_ID + "', name: 'Bat Milko'})\n"
            + "SET client1:FirstPartyFraudster\n"
            + "SET client3:Mule\n"
            + "CREATE (transfer1:Transaction {id: 'tx-1', globalStep: 1, step: 1, ts: 1, fraud: false, amount: " + TRANSFER1_AMOUNT + "})\n"
            + "SET transfer1:Transfer\n"
            + "CREATE (transfer2:Transaction {id: 'tx-2', globalStep: 803862, step: 719, ts: 1, fraud: false, amount:" + TRANSFER2_AMOUNT + "})\n"
            + "SET transfer2:Transfer\n"
            + "CREATE (transfer3:Transaction {id: 'tx-11', globalStep: 803863, step: 720, ts: 1, fraud: true, amount: " + TRANSFER3_AMOUNT +  "})\n"
            + "SET transfer3:Transfer\n"
            + "MERGE (client1)-[:PERFORMED]->(transfer1)-[:TO]->(client4)"
            + "MERGE (client4)-[:PERFORMED]->(transfer2)-[:TO]->(client1)"
            + "MERGE (client4)-[:PERFORMED]->(transfer3)-[:TO]->(client1)"
            + "MERGE (client1)-[:TRANSFER_TO {amount: " + TRANSFER1_AMOUNT +"}]->(client4)"
            + "MERGE (client4)-[:TRANSFER_TO {amount: " + TRANSFER2_AMOUNT + TRANSFER3_AMOUNT +"}]->(client1)";

    private static final String CREATE_CLIENTS_WITH_FRAUD_GROUP_AND_TRANSFER_TO_RELATIONSHIPS = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "CREATE (client4:Client {id: '" + CLIENT4_ID + "', name: 'Bat Milko'})\n"
            + "SET client1:FirstPartyFraudster\n"
            + "SET client1.secondPartyFraudGroup=0\n"
            + "SET client4.secondPartyFraudGroup=0\n"
            + "SET client3:Mule\n"
            + "CREATE (transfer1:Transaction {id: 'tx-1', globalStep: 1, step: 1, ts: 1, fraud: false, amount: " + TRANSFER1_AMOUNT + "})\n"
            + "SET transfer1:Transfer\n"
            + "CREATE (transfer2:Transaction {id: 'tx-2', globalStep: 803862, step: 719, ts: 1, fraud: false, amount:" + TRANSFER2_AMOUNT + "})\n"
            + "SET transfer2:Transfer\n"
            + "CREATE (transfer3:Transaction {id: 'tx-11', globalStep: 803863, step: 720, ts: 1, fraud: true, amount: " + TRANSFER3_AMOUNT +  "})\n"
            + "SET transfer3:Transfer\n"
            + "MERGE (client1)-[:PERFORMED]->(transfer1)-[:TO]->(client4)"
            + "MERGE (client4)-[:PERFORMED]->(transfer2)-[:TO]->(client1)"
            + "MERGE (client4)-[:PERFORMED]->(transfer3)-[:TO]->(client1)"
            + "MERGE (client1)-[:TRANSFER_TO {amount: " + TRANSFER1_AMOUNT +"}]->(client4)"
            + "MERGE (client4)-[:TRANSFER_TO {amount: " + TRANSFER2_AMOUNT + TRANSFER3_AMOUNT +"}]->(client1)";

    private static final String DELETE_TRANSACTION = "MATCH (n) DETACH DELETE n";
    private static final String TRANSFER_TO_RELATIONSHIP = "TRANSFER_TO";
    private static final String FRAUD_NETWORK_GRAPH_PROJECTION = "SecondPartyFraudNetwork";

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
        TaskFactory.getDropGraphProjectionTask(FRAUD_NETWORK_GRAPH_PROJECTION).execute(neo4jClient);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_WITH_TRANSACTIONS));
        }
    }

    @Test
    void should_create_relationship_from_fraudster_to_client_with_the_transferred_amount_of_money() {
        Task task = TaskFactory.getCreateRelationshipsFromFraudstersToClientsTask(TRANSFER_TO_RELATIONSHIP);
        assertTrue(task.execute(neo4jClient));
        System.out.println(task.getSuccessMessage());
        assertTrue(task.getSuccessMessage().matches("Successfully created 1 relationship and set 1 property\\."));
        assertEquals(TRANSFER1_AMOUNT, getTransferredAmount());
        assertEquals(CLIENT4_ID, getSecondPartyFraudSuspectId());
    }

    @Test
    void should_create_relationship_from_client_to_fraudster_with_the_transferred_amount_of_money() {
        Task task = TaskFactory.getCreateRelationshipsFromClientsToFraudsters(TRANSFER_TO_RELATIONSHIP);
        assertTrue(task.execute(neo4jClient));
        System.out.println(task.getSuccessMessage());
        assertTrue(task.getSuccessMessage().matches("Successfully created 1 relationship and set 1 property\\."));
        assertEquals(TRANSFER2_AMOUNT + TRANSFER3_AMOUNT, getTransferredAmount());
        assertEquals(CLIENT4_ID, getSecondPartyFraudSuspectId());
    }

    private double getTransferredAmount() {
        return neo4jClient.query("MATCH (c1:Client)-[rel:TRANSFER_TO]->(c2:Client) " +
                "RETURN rel.amount AS amount")
                .fetchAs(Double.class)
                .mappedBy((typeSystem, record) -> record.get("amount").asDouble())
                .one().orElseThrow();
    }

    private String getSecondPartyFraudSuspectId() {
        return neo4jClient.query("MATCH (c:Client) " +
                "WHERE c:SecondPartyFraudSuspect " +
                "RETURN c.id AS clientId")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("clientId").asString())
                .one().orElseThrow();
    }

    @Test
    void should_identify_second_party_fraud_cluster(@Autowired Driver driver) {
        recreateNodes(driver, CREATE_CLIENTS_WITH_TRANSACTIONS_AND_TRANSFER_TO_RELATIONSHIPS);
        Task createProjectionTask = TaskFactory.getCreateSecondPartyFraudNetworkGraphProjectionTask(FRAUD_NETWORK_GRAPH_PROJECTION, TRANSFER_TO_RELATIONSHIP);
        assertTrue(createProjectionTask.execute(neo4jClient));

        Task task = TaskFactory.getCommunityDetectionAlgorithmForSecondPartyFraudGroupingTask("wcc", FRAUD_NETWORK_GRAPH_PROJECTION);
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Successfully executed the algorithm 'Weakly Connected Components': propertiesSet=2\\."));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT1_ID, "secondPartyFraudGroup"));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT4_ID, "secondPartyFraudGroup"));
        assertEquals(NULL, getClientNodeProperty(CLIENT2_ID, "secondPartyFraudGroup"));
        assertEquals(NULL, getClientNodeProperty(CLIENT3_ID, "secondPartyFraudGroup"));
    }

    @Test
    void should_assign_second_party_fraud_score_to_suspects(@Autowired Driver driver) {
        recreateNodes(driver, CREATE_CLIENTS_WITH_FRAUD_GROUP_AND_TRANSFER_TO_RELATIONSHIPS);
        Task createProjectionTask = TaskFactory.getCreateSecondPartyFraudNetworkGraphProjectionTask(FRAUD_NETWORK_GRAPH_PROJECTION, TRANSFER_TO_RELATIONSHIP);
        assertTrue(createProjectionTask.execute(neo4jClient));

        Task task = TaskFactory.getCentralityAlgorithmForSecondPartyFraudScore("pageRank", FRAUD_NETWORK_GRAPH_PROJECTION, 0.5);
        task.execute(neo4jClient);
        assertTrue(task.getSuccessMessage().matches("Successfully executed the algorithm 'Page Rank': propertiesSet=1\\."));

        assertEquals(NULL, getClientNodeProperty(CLIENT1_ID, "secondPartyFraudScore"));
        assertEquals(NULL, getClientNodeProperty(CLIENT2_ID, "secondPartyFraudScore"));
        assertEquals(NULL, getClientNodeProperty(CLIENT3_ID, "secondPartyFraudScore"));
        assertNotEquals(NULL, getClientNodeProperty(CLIENT4_ID, "secondPartyFraudScore"));
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
}
