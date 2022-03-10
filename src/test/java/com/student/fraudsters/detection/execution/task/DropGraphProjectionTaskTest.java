package com.student.fraudsters.detection.execution.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@DataNeo4jTest
public class DropGraphProjectionTaskTest {

    private static final String TEST_PROJECTION_NAME = "TestProjection";
    private static final String CREATE_CLIENTS_WITH_IDENTITIES = ""
            + "CREATE (client1:Client {id: '123456', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '234567', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '654321', name: 'The Mule'})\n"
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
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_WITH_IDENTITIES));
        }
    }

    @Test
    void should_delete_the_existing_graph_projection() {
        CreateGraphProjectionTask createGraphProjectionTask = TaskFactory.getCreateGraphProjectionForClientsWithSharedIdentifiersTask(TEST_PROJECTION_NAME);
        createGraphProjectionTask.execute(neo4jClient);
        DropGraphProjectionTask task = TaskFactory.getDropGraphProjectionTask(TEST_PROJECTION_NAME);
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Successfully deleted graph projection '" + TEST_PROJECTION_NAME + "'\\."));
    }

    @Test
    void should_detect_already_deleted_graph_projection() {
        DropGraphProjectionTask task = TaskFactory.getDropGraphProjectionTask(TEST_PROJECTION_NAME);
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Graph projection '" + TEST_PROJECTION_NAME + "' is already deleted\\."));
    }
}
