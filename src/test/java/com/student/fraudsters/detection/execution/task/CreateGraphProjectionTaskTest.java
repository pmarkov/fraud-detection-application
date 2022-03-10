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


import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@DataNeo4jTest
public class CreateGraphProjectionTaskTest {

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
    private static final String DROP_TEST_PROJECTION = "CALL gds.graph.drop('" + TEST_PROJECTION_NAME + "');";

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
    void should_create_graph_projection(@Autowired Driver driver) {
        CreateGraphProjectionTask task = TaskFactory.getCreateGraphProjectionForClientsWithSharedIdentifiersTask(TEST_PROJECTION_NAME);
        task.execute(neo4jClient);
        assertTrue(task.getSuccessMessage().matches("Successfully created '"+ TEST_PROJECTION_NAME + "', nodeCount=3, relationshipCount=1, createMillis=\\d+\\."));
        assertTestProjectionExists(driver);
        dropTestProjection(driver);
    }

    @Test
    void should_create_graph_projection_on_empty_database(@Autowired Driver driver) {
        CreateGraphProjectionTask task = TaskFactory.getCreateGraphProjectionForClientsWithSharedIdentifiersTask(TEST_PROJECTION_NAME);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
        }
        assertTrue(task.execute(neo4jClient));
        assertTrue(task.getSuccessMessage().matches("Successfully created '"+ TEST_PROJECTION_NAME + "', nodeCount=0, relationshipCount=0, createMillis=\\d+\\."));
        assertTestProjectionExists(driver);
        dropTestProjection(driver);
    }

    @Test
    void should_fail_to_create_graph_projection_with_existing_name(@Autowired Driver driver) {
        CreateGraphProjectionTask task = TaskFactory.getCreateGraphProjectionForClientsWithSharedIdentifiersTask(TEST_PROJECTION_NAME);
        assertTrue(task.execute(neo4jClient));
        assertTestProjectionExists(driver);

        assertFalse(task.execute(neo4jClient));
        assertTrue(task.getErrorMessage().matches(".*A graph with name '" + TEST_PROJECTION_NAME + "' already exists.*"));
        dropTestProjection(driver);
    }

    private void assertTestProjectionExists(Driver driver) {
        try (Session session = driver.session()) {
            String actualProjectionName = session.readTransaction(tx -> tx.run("CALL gds.graph.list();").single().get("graphName").asString());
            assertEquals(TEST_PROJECTION_NAME, actualProjectionName);
        }
    }

    private void dropTestProjection(Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DROP_TEST_PROJECTION));
        }
    }
}
