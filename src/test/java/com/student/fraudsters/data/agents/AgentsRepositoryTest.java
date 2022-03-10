package com.student.fraudsters.data.agents;

import com.student.fraudsters.data.entity.agents.*;
import com.student.fraudsters.data.entity.identity.Email;
import com.student.fraudsters.data.entity.identity.Phone;
import com.student.fraudsters.data.entity.identity.SSN;
import com.student.fraudsters.data.repositories.agents.BankRepository;
import com.student.fraudsters.data.repositories.agents.ClientRepository;
import com.student.fraudsters.data.repositories.agents.MerchantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DataNeo4jTest
public class AgentsRepositoryTest {

    private static final String CLIENT1_ID = "123456";
    private static final String CLIENT2_ID = "234567";
    private static final String CLIENT3_ID = "654321";
    private static final String BANK1_ID = "B60-0000667";
    private static final String BANK2_ID = "123456";
    private static final String MERCHANT1_ID = "03-0000524";
    private static final String MERCHANT2_ID = "123456";

    private static final String CREATE_CLIENTS_QUERY = ""
            + "CREATE (client1:Client {id: '" + CLIENT1_ID + "', name: 'Luke Pitts'})\n"
            + "CREATE (client2:Client {id: '" + CLIENT2_ID + "', name: 'Morgan Freeman'})\n"
            + "CREATE (client3:Client {id: '" + CLIENT3_ID + "', name: 'The Mule'})\n"
            + "SET client3:Mule\n"
            + "SET client1.firstPartyFraudGroup = 1\n"
            + "SET client2.firstPartyFraudGroup = 2\n"
            + "SET client3.firstPartyFraudGroup = 2\n"
            + "SET client2:SecondPartyFraudSuspect\n"
            + "SET client2.secondPartyFraudScore=2.5\n"
            + "SET client3.secondPartyFraudScore=0.5\n"
            + "CREATE (phone1:Phone {phoneNumber: '853-637-949'})\n"
            + "CREATE (phone2:Phone {phoneNumber: '123-456-789'})\n"
            + "CREATE (ssn1:SSN {ssn: '330-39-0677'})\n"
            + "CREATE (email1:Email {email: 'zoeyhorn330@yahoo.com'})\n"
            + "MERGE (client1)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client2)-[:HAS_PHONE]->(phone1)\n"
            + "MERGE (client3)-[:HAS_PHONE]->(phone2)\n"
            + "MERGE (client1)-[:HAS_SSN]->(ssn1)\n"
            + "MERGE (client1)-[:HAS_EMAIL]->(email1)\n";

    private static final String CREATE_BANKS_AND_MERCHANTS_QUERY = ""
            + "CREATE (bank1:Bank {id: '" + BANK1_ID + "', name: 'Bank of Mccoy'})\n"
            + "CREATE (bank2:Bank {id: '" + BANK2_ID + "', name: 'National Bank'})\n"
            + "CREATE (merchant1:Merchant {id: '" + MERCHANT1_ID + "', name: 'MYrsa'})\n"
            + "CREATE (merchant2:Merchant {id: '" + MERCHANT2_ID + "', name: 'SomeMerchant'})\n";

    private static final String DELETE_TRANSACTION = "MATCH (n) DETACH DELETE n";

    @Container
    private static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.2");

    @DynamicPropertySource
    static void setNeo4jContainer(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @BeforeEach
    void setUp(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(CREATE_CLIENTS_QUERY));
            session.writeTransaction(tx -> tx.run(CREATE_BANKS_AND_MERCHANTS_QUERY));
        }
    }

    @AfterEach
    void tearDown(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
        }
    }

    @Test
    void should_match_client_by_id(@Autowired ClientRepository repository) {
        assertThat(repository.findClientById(CLIENT1_ID))
                .isNotNull()
                .extracting(BaseAgent::getName)
                .isEqualTo("Luke Pitts");
        assertThat(repository.findClientById(CLIENT2_ID))
                .isNotNull()
                .extracting(BaseAgent::getName)
                .isEqualTo("Morgan Freeman");
        assertThat(repository.findClientById(CLIENT3_ID))
                .isNotNull()
                .extracting(BaseAgent::getName)
                .isEqualTo("The Mule");
        assertThat(repository.findClientById("345678"))
                .isNull();
    }

    @Test
    void should_count_clients(@Autowired ClientRepository repository) {
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    void should_count_clients_with_shared_identifiers(@Autowired ClientRepository repository) {
        assertThat(repository.countClientsWithSharedIdentifiers()).isEqualTo(2);
    }

    @Test
    void should_find_clients_by_first_party_fraud_group_id(@Autowired ClientRepository repository) {
        List<ClientDto> firstPartyFraudClients = repository.findClientsByFirstPartyFraudGroup(1);
        assertThat(firstPartyFraudClients).hasSize(1);
        assertEquals(CLIENT1_ID, firstPartyFraudClients.get(0).getId());
    }

    @Test
    void should_find_clients_in_fraud_rings(@Autowired ClientRepository repository) {
        List<ClientDto> firstPartyFraudClients = repository.findClientsInFraudRings();
        assertThat(firstPartyFraudClients)
                .hasSize(3);
        assertThat(firstPartyFraudClients.get(0))
                .extracting(BaseAgent::getId)
                .isEqualTo(CLIENT1_ID);
        assertThat(firstPartyFraudClients.get(1))
                .extracting(BaseAgent::getId)
                .isEqualTo(CLIENT2_ID);
        assertThat(firstPartyFraudClients.get(2))
                .extracting(BaseAgent::getId)
                .isEqualTo(CLIENT3_ID);
    }

    @Test
    void should_get_fraud_rings_by_size(@Autowired ClientRepository repository) {
        List<FraudRingDto> fraudRings = repository.getFraudRingsOrderedBySize(1);
        assertThat(fraudRings)
                .hasSize(2);
        assertThat(fraudRings.get(0).getGroupId()).isEqualTo(2);
        assertThat(fraudRings.get(1).getGroupId()).isEqualTo(1);
        assertThat(fraudRings.get(0).getGroupSize()).isEqualTo(2);
        assertThat(fraudRings.get(1).getGroupSize()).isEqualTo(1);
    }

    @Test
    void should_get_fraud_rings_by_size_and_minimum_size(@Autowired ClientRepository repository) {
        List<FraudRingDto> fraudRings = repository.getFraudRingsOrderedBySize(2);
        assertThat(fraudRings)
                .hasSize(1);
        assertThat(fraudRings.get(0).getGroupId()).isEqualTo(2);
    }

    @Test
    void should_not_get_fraud_rings_if_their_size_is_lower_than_the_minimum(@Autowired ClientRepository repository) {
        List<FraudRingDto> fraudRings = repository.getFraudRingsOrderedBySize(3);
        assertThat(fraudRings)
                .hasSize(0);
    }

    @Test
    void should_get_second_party_fraud_suspect(@Autowired ClientRepository repository) {
        List<ClientDto> secondPartyFraudSuspects = repository.findSecondPartyFraudSuspects();
        assertThat(secondPartyFraudSuspects)
                .hasSize(1);
        assertEquals(CLIENT2_ID, secondPartyFraudSuspects.get(0).getId());
    }

    @Test
    void should_get_second_party_fraudsters_by_fraud_score(@Autowired ClientRepository repository) {
        List<ClientDto> secondPartyFraudsters = repository.findSecondPartyFraudstersByFraudScore(0.5);
        assertThat(secondPartyFraudsters)
                .hasSize(1);
        assertEquals(CLIENT2_ID, secondPartyFraudsters.get(0).getId());
    }

    @Test
    void should_count_mules_by_id(@Autowired ClientRepository repository) {
        assertThat(repository.countMules()).isEqualTo(1);
    }

    @Test
    void should_match_phone_relationships(@Autowired ClientRepository repository) {
        Client client1 = repository.findClientById(CLIENT1_ID);
        Client client2 = repository.findClientById(CLIENT2_ID);
        Client client3 = repository.findClientById(CLIENT3_ID);
        assertThat(client1.getPhone())
                .isNotNull()
                .extracting(Phone::getPhoneNumber)
                .isEqualTo("853-637-949");
        assertThat(client2.getPhone())
                .isNotNull()
                .extracting(Phone::getPhoneNumber)
                .isEqualTo("853-637-949");
        assertThat(client3.getPhone())
                .isNotNull()
                .extracting(Phone::getPhoneNumber)
                .isEqualTo("123-456-789");
    }

    @Test
    void should_match_ssn_relationships(@Autowired ClientRepository repository) {
        Client client1 = repository.findClientById(CLIENT1_ID);
        assertThat(client1.getSsn())
                .isNotNull()
                .extracting(SSN::getSsn)
                .isEqualTo("330-39-0677");
    }

    @Test
    void should_match_email_relationships(@Autowired ClientRepository repository) {
        Client client1 = repository.findClientById(CLIENT1_ID);
        assertThat(client1.getEmail())
                .isNotNull()
                .extracting(Email::getEmail)
                .isEqualTo("zoeyhorn330@yahoo.com");
    }

    @Test
    void should_match_bank_by_id(@Autowired BankRepository repository) {
        assertThat(repository.findBankById(BANK1_ID))
                .isNotNull()
                .extracting(Bank::getName)
                .isEqualTo("Bank of Mccoy");
        assertThat(repository.findBankById(BANK2_ID))
                .isNotNull()
                .extracting(Bank::getName)
                .isEqualTo("National Bank");
        assertThat(repository.findBankById(CLIENT3_ID))
                .isNull();
    }

    @Test
    void should_count_banks_by_id(@Autowired BankRepository repository) {
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void should_match_merchant_by_id(@Autowired MerchantRepository repository) {
        assertThat(repository.findMerchantById(MERCHANT1_ID))
                .isNotNull()
                .extracting(Merchant::getName)
                .isEqualTo("MYrsa");
        assertThat(repository.findMerchantById(MERCHANT2_ID))
                .isNotNull()
                .extracting(Merchant::getName)
                .isEqualTo("SomeMerchant");
        assertThat(repository.findMerchantById(CLIENT3_ID))
                .isNull();
    }

    @Test
    void should_count_merchants_by_id(@Autowired MerchantRepository repository) {
        assertThat(repository.count()).isEqualTo(2);
    }
}
