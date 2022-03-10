package com.student.fraudsters.data.repositories.transactions;

import com.student.fraudsters.data.entity.transactions.TransactionMarketStatisticsDto;
import com.student.fraudsters.data.entity.transactions.TransactionFrequenciesDto;
import org.neo4j.driver.Record;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Collection;

public class TransactionStatisticsImpl implements TransactionStatistics {

    public static final String TRANSACTION_FREQUENCIES =
            "MATCH (t:Transaction)\n" +
            "WITH count(t) AS allTransactions\n" +
            "UNWIND ['CashIn', 'CashOut', 'Payment', 'Transfer', 'Debit'] AS txType\n" +
            "  CALL apoc.cypher.run(\n" +
            "    \"MATCH (t:\" + txType + \")\n" +
            "    RETURN count(t) AS txCount\", {})\n" +
            "  YIELD value\n" +
            "WHERE allTransactions > 0\n" +
            "WITH *, value.txCount AS absFrequency\n" +
            "RETURN txType,\n" +
            "       (toFloat(absFrequency)/allTransactions)*100 AS relFrequency,\n" +
            "       absFrequency";

    public static final String TRANSACTION_MARKET_METRICS_QUERY =
            "MATCH (t:Transaction)\n" +
            "WITH sum(t.amount) AS globalSum, count(t) AS globalCount\n" +
            "UNWIND ['CashIn', 'CashOut', 'Payment', 'Debit', 'Transfer'] AS txType\n" +
            "  CALL apoc.cypher.run('MATCH (t:' + txType + ')\n" +
            "    RETURN sum(t.amount) as txAmount, count(t) AS txCount', {})\n" +
            "  YIELD value\n" +
            "WHERE globalSum > 0 AND globalCount > 0\n" +
            "WITH *,\n" +
            "   CASE value.txCount\n" +
            "       WHEN 0 THEN 1\n" +
            "       ELSE value.txCount\n" +
            "   END AS nonZeroTxCount\n" +
            "RETURN txType,\n" +
            "   value.txAmount AS totalMarketValue,\n" +
            "   round(100*toFloat(value.txAmount)/toFloat(globalSum), 1) AS marketValuePercent,\n" +
            "   round(100*toFloat(value.txCount)/toFloat(globalCount), 1) AS marketTransactionsPercent,\n" +
            "   toInteger(toFloat(value.txAmount)/toFloat(nonZeroTxCount)) AS avgTransactionValue,\n" +
            "   value.txCount AS numberOfTransactions\n";

    private final Neo4jClient neo4jClient;

    public TransactionStatisticsImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public Collection<TransactionFrequenciesDto> getTransactionFrequencies() {
         return this.neo4jClient
                 .query(TRANSACTION_FREQUENCIES)
                 .fetchAs(TransactionFrequenciesDto.class)
                 .mappedBy(((typeSystem, record) -> toTransactionFrequenciesDto(record)))
                 .all();
    }

    private TransactionFrequenciesDto toTransactionFrequenciesDto(Record record) {
        return new TransactionFrequenciesDto(
                record.get("txType").asString(),
                record.get("absFrequency").asLong(),
                record.get("relFrequency").asDouble()
        );
    }

    @Override
    public Collection<TransactionMarketStatisticsDto> getTransactionMarketMetrics() {
        return this.neo4jClient
                .query(TRANSACTION_MARKET_METRICS_QUERY)
                .fetchAs(TransactionMarketStatisticsDto.class)
                .mappedBy(((typeSystem, record) -> toTransactionMarketStatisticsDto(record)))
                .all();
    }

    private TransactionMarketStatisticsDto toTransactionMarketStatisticsDto(Record record) {
        return new TransactionMarketStatisticsDto(
                record.get("txType").asString(),
                record.get("totalMarketValue").asDouble(),
                record.get("marketValuePercent").asDouble(),
                record.get("marketTransactionsPercent").asDouble(),
                record.get("avgTransactionValue").asLong(),
                record.get("numberOfTransactions").asLong()
        );
    }
}
