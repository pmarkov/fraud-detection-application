package com.student.fraudsters.data.transactions;

import com.student.fraudsters.data.entity.transactions.TransactionFrequenciesDto;
import com.student.fraudsters.data.entity.transactions.TransactionMarketStatisticsDto;
import com.student.fraudsters.services.TransactionService;
import com.student.fraudsters.data.entity.transactions.Transaction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
public class TransactionServiceTest {


    private static final String CREATE_TRANSACTIONS = ""
            + "CREATE (transfer1:Transaction {id: 'tx-1', globalStep: 1, step: 1, ts: 1, fraud: false, amount: 129105.89322})\n"
            + "SET transfer1:Transfer\n"
            + "CREATE (transfer2:Transaction {id: 'tx-2', globalStep: 803862, step: 719, ts: 1, fraud: false, amount: 129105})\n"
            + "SET transfer2:Transfer\n"
            + "CREATE (transfer3:Transaction {id: 'tx-11', globalStep: 803863, step: 720, ts: 1, fraud: true, amount: 129105})\n"
            + "SET transfer3:Transfer\n"
            + "CREATE (payment:Transaction {id: 'tx-3', globalStep: 2, step: 1, ts: 15, fraud: false, amount: 9105.89})\n"
            + "SET payment:Payment\n"
            + "CREATE (cashIn1:Transaction {id: 'tx-4', globalStep: 105, step: 0, ts: 1, fraud: false, amount: 105.89322})\n"
            + "SET cashIn1:CashIn\n"
            + "CREATE (cashIn2:Transaction {id: 'tx-5', globalStep: 4, step: 2, ts: 1, fraud: false, amount: 129105.89322})\n"
            + "SET cashIn2:CashIn\n"
            + "CREATE (cashOut:Transaction {id: 'tx-6', globalStep: 105, step: 1, ts: 1, fraud: false, amount: 1005.89322})\n"
            + "SET cashOut:CashOut\n";
    public static final String DELETE_TRANSACTION = "MATCH (n) DETACH DELETE n";

    @Container
    private static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.2")
            .withPlugins(MountableFile.forClasspathResource("/plugins"));

    @DynamicPropertySource
    static void setNeo4jContainer(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @Autowired
    TransactionService transactionService;

    @BeforeEach
    void setUp(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(CREATE_TRANSACTIONS));
        }
    }

    @AfterEach
    void tearDown(@Autowired Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(DELETE_TRANSACTION));
        }
    }

    @Test
    void should_count_all_transactions() {
        long expectedCount = 7;
        assertThat(transactionService.countAllTransactions().get("result")).isEqualTo(expectedCount);
    }

    @Test
    void should_count_transfer_transactions() {
        long expectedTransfers = 3;
        assertThat(transactionService.countTransferTransactions().get("result")).isEqualTo(expectedTransfers);
    }

    @Test
    void should_count_payment_transactions() {
        long expectedPayments = 1;
        assertThat(transactionService.countPaymentTransactions().get("result")).isEqualTo(expectedPayments);
    }

    @Test
    void should_count_cash_in_transactions() {
        long expectedCashIns = 2;
        assertThat(transactionService.countCashInTransactions().get("result")).isEqualTo(expectedCashIns);
    }

    @Test
    void should_count_cash_out_transactions() {
        long expectedCashOuts = 1;
        assertThat(transactionService.countCashOutTransactions().get("result")).isEqualTo(expectedCashOuts);
    }

    @Test
    void should_count_debit_transactions() {
        long expectedDebits = 0;
        assertThat(transactionService.countDebitTransactions().get("result")).isEqualTo(expectedDebits);
    }

    @Test
    void should_get_transaction_frequencies_from_repo() {
        var result = transactionService.getTransactionFrequencies();
        Map<String, TransactionFrequenciesDto> frequenciesByType = new HashMap<>();
        result.forEach(txFrequenciesDto -> frequenciesByType.put(txFrequenciesDto.getTransactionType(), txFrequenciesDto));

        testFrequencyValues(frequenciesByType.get("Debit"), 0L, 0.0);
        testFrequencyValues(frequenciesByType.get("Payment"), 1L, 14.286);
        testFrequencyValues(frequenciesByType.get("CashOut"), 1L, 14.286);
        testFrequencyValues(frequenciesByType.get("CashIn"), 2L, 28.571);
        testFrequencyValues(frequenciesByType.get("Transfer"), 3L, 42.857);
    }

    private void testFrequencyValues(TransactionFrequenciesDto frequencies, long expectedAbsoluteFrequency, double expectedRelativeFrequency) {
        assertThat(frequencies.getAbsoluteFrequency()).isEqualTo(expectedAbsoluteFrequency);
        assertEquals(expectedRelativeFrequency, frequencies.getRelativeFrequency(), 0.001);
    }

    @Test
    void should_not_get_transaction_frequencies_on_empty_database(@Autowired Neo4jTemplate template) {
        template.deleteAll(Transaction.class);
        assertThat(transactionService.countAllTransactions().get("result")).isEqualTo(0L);
        Assertions.assertThat(transactionService.getTransactionFrequencies()).isEmpty();
    }

    @Test
    void should_get_transaction_market_metrics_from_repo() {
        var result = transactionService.getTransactionMarketMetrics();

        Map<String, TransactionMarketStatisticsDto> resultsByType = new HashMap<>();
        result.forEach( txStatisticsDto -> resultsByType.put(txStatisticsDto.getTransactionType(), txStatisticsDto));

        test_metrics(resultsByType.get("Debit"), 0.0, 0.0, 0L, 0L, 0.0);
        test_metrics(resultsByType.get("Payment"), 9105.89, 14.3, 9105L, 1L, 1.7);
        test_metrics(resultsByType.get("CashOut"), 1005.893, 14.3, 1005L, 1L, 0.2);
        test_metrics(resultsByType.get("CashIn"), 129_211.786, 28.6, 64605L, 2L, 24.5);
        test_metrics(resultsByType.get("Transfer"), 387_315.893, 42.9, 129105L, 3L, 73.5);
    }

    void test_metrics(TransactionMarketStatisticsDto metrics, double totalMarketValue, double marketTransactionPercent,
                      long avgTransactionValue, long numberOfTransactions, double marketValuePercent) {

        assertEquals(totalMarketValue, metrics.getTotalMarketValue(), 0.001);
        assertThat(metrics.getMarketTransactionsPercent()).isEqualTo(marketTransactionPercent);
        assertThat(metrics.getAvgTransactionValue()).isEqualTo(avgTransactionValue);
        assertThat(metrics.getNumberOfTransactions()).isEqualTo(numberOfTransactions);
        assertThat(metrics.getMarketValuePercent()).isEqualTo(marketValuePercent);
    }

    @Test
    void should_not_get_transaction_market_metrics_on_empty_database(@Autowired Neo4jTemplate template) {
        template.deleteAll(Transaction.class);
        assertThat(transactionService.countAllTransactions().get("result")).isEqualTo(0L);
        Assertions.assertThat(transactionService.getTransactionMarketMetrics()).isEmpty();
    }
}
