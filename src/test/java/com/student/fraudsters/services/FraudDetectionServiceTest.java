package com.student.fraudsters.services;


import com.student.fraudsters.detection.exception.NotAvailableAlgorithmException;
import com.student.fraudsters.detection.execution.CypherConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@Testcontainers
@SpringBootTest
public class FraudDetectionServiceTest {

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
            + "MERGE (client1)-[:HAS_EMAIL]->(email1)\n";

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

    private static final String DELETE_TRANSACTION = "MATCH (n) DETACH DELETE n";

    @Autowired
    FraudDetectionService fraudDetectionService;

    @Autowired
    Neo4jClient client;

    SseEmitter sseEmitter = mock(SseEmitter.class);

    @BeforeEach
    void setUp(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_WITH_IDENTITIES));
        }
    }

    @Test
    void should_verify_that_the_algorithm_exists() {
        fraudDetectionService.verifyExistingAlgorithmEntries(
                sseEmitter,
                "wcc",
                CypherConstants.getCommunityDetectionAlgorithms());
    }

    @Test
    void should_fail_verification_that_the_algorithm_exists() {
//        Mockito.when(sseEmitter.completeWithError(any(Throwable.class))).then(Answers.RETURNS_MOCKS);
        Assertions.assertThrows(NotAvailableAlgorithmException.class, () -> fraudDetectionService.verifyExistingAlgorithmEntries(
                sseEmitter,
                "ala-bala",
                CypherConstants.getCommunityDetectionAlgorithms()));
    }

    @Test
    void should_execute_first_party_fraud_detection() {
        fraudDetectionService.executeFirstPartyFraudDetection(
                sseEmitter,
                "wcc",
                "nodeSimilarity",
                "pageRank",
                0.4
        );
    }

    @Test
    void should_execute_second_party_fraud_detection() {
        fraudDetectionService.executeSecondPartyFraudDetection(
                sseEmitter,
                "wcc",
                "pageRank",
                0.5
        );
    }
}
