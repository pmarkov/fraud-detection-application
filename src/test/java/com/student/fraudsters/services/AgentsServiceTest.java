package com.student.fraudsters.services;


import com.student.fraudsters.data.entity.agents.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class AgentsServiceTest {

    private final static String CLIENT1_ID = "123456";
    private final static String CLIENT2_ID = "234567";
    private final static String CLIENT3_ID = "654321";
    private final static String CLIENT4_ID = "567890";
    private static final String BANK1_ID = "B60-0000667";
    private static final String BANK2_ID = "123456";
    private static final String MERCHANT1_ID = "03-0000524";
    private static final String MERCHANT2_ID = "123456";

    private static final String CREATE_CLIENTS_WITH_IDENTITIES_AND_FRAUD_GROUP = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts', firstPartyFraudGroup: 1})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman', firstPartyFraudGroup: 1})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule', firstPartyFraudGroup: 2, secondPartyFraudScore: 0.4})\n"
            + "CREATE (client4:Client {id: '" + CLIENT4_ID + "', name: 'Bat Milko', secondPartyFraudScore: 1.4})\n"
            + "SET client1:FirstPartyFraudster\n"
            + "SET client4:SecondPartyFraudSuspect\n"
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

    private static final String CREATE_BANKS_AND_MERCHANTS_QUERY = ""
            + "CREATE (bank1:Bank {id: '" + BANK1_ID + "', name: 'Bank of Mccoy'})\n"
            + "CREATE (bank2:Bank {id: '" + BANK2_ID + "', name: 'National Bank'})\n"
            + "CREATE (merchant1:Merchant {id: '" + MERCHANT1_ID + "', name: 'MYrsa'})\n"
            + "CREATE (merchant2:Merchant {id: '" + MERCHANT2_ID + "', name: 'SomeMerchant'})\n";

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
    AgentsService agentsService;

    @BeforeEach
    void setUp(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_WITH_IDENTITIES_AND_FRAUD_GROUP));
            session.writeTransaction(tx -> tx.run(CREATE_BANKS_AND_MERCHANTS_QUERY));
        }
    }

    @Test
    void should_return_client_by_id() {
        Client client = agentsService.getClient(CLIENT1_ID);
        assertThat(client).isNotNull();
        assertThat(client.getName()).isEqualTo("Luke Pitts");
        assertThat(client.getFirstPartyFraudGroup()).isEqualTo(1);
    }

    @Test
    void should_return_client_count() {
        assertThat(agentsService.getClientCount().get("result")).isEqualTo(4);
    }

    @Test
    void should_return_bank_by_id() {
        Bank bank = agentsService.getBank(BANK1_ID);
        assertThat(bank).isNotNull();
        assertThat(bank.getName()).isEqualTo("Bank of Mccoy");
    }

    @Test
    void should_return_bank_count() {
        assertThat(agentsService.getBankCount().get("result")).isEqualTo(2);
    }

    @Test
    void should_return_merchant_by_id() {
        Merchant merchant = agentsService.getMerchant(MERCHANT1_ID);
        assertThat(merchant).isNotNull();
        assertThat(merchant.getName()).isEqualTo("MYrsa");
    }

    @Test
    void should_return_merchant_count() {
        assertThat(agentsService.getMerchantCount().get("result")).isEqualTo(2);
    }

    @Test
    void should_return_count_of_clients_with_shared_identifiers() {
        assertThat(agentsService.getCountOfClientsWithSharedIdentifiers().get("result")).isEqualTo(2);
    }

    @Test
    void should_return_2_fraud_rings_with_minimum_size_of_1() {
        List<FraudRingDto> fraudRings = agentsService.getFraudRingsOrderedBySize(1);
        assertThat(fraudRings).hasSize(2);
        assertThat(fraudRings.get(0).getGroupId()).isEqualTo(1);
        assertThat(fraudRings.get(0).getGroupSize()).isEqualTo(2);
        assertThat(fraudRings.get(1).getGroupId()).isEqualTo(2);
        assertThat(fraudRings.get(1).getGroupSize()).isEqualTo(1);
    }

    @Test
    void should_return_1_fraud_ring_with_minimum_size_of_2() {
        List<FraudRingDto> fraudRings = agentsService.getFraudRingsOrderedBySize(2);
        assertThat(fraudRings).hasSize(1);
        assertThat(fraudRings.get(0).getGroupId()).isEqualTo(1);
        assertThat(fraudRings.get(0).getGroupSize()).isEqualTo(2);
    }

    @Test
    void should_return_all_clients_first_party_fraud_rings() {
        assertThat(agentsService.getAllClientsInFirstPartyFraudRing()).hasSize(3);
    }

    @Test
    void should_return_clients_in_specific_first_party_fraud_ring() {
        assertThat(agentsService.getClientsInFirstPartyFraudRing(1)).hasSize(2);
    }

    @Test
    void should_return_first_party_fraudsters() {
        List<ClientDto> clients = agentsService.getFirstPartyFraudsters();
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getName()).isEqualTo("Luke Pitts");
    }

    @Test
    void should_return_second_party_fraud_suspects() {
        List<ClientDto> clients = agentsService.getSecondPartyFraudSuspects();
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getName()).isEqualTo("Bat Milko");
    }

    @Test
    void should_return_second_party_fraudsters_by_score() {
        List<ClientDto> clients = agentsService.getSecondPartyFraudstersByFraudScore(0.3);
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getName()).isEqualTo("Bat Milko");
    }
}
