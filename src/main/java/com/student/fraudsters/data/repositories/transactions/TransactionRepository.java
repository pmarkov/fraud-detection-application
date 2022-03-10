package com.student.fraudsters.data.repositories.transactions;

import com.student.fraudsters.data.entity.transactions.Transaction;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;

public interface TransactionRepository extends Repository<Transaction, String>, TransactionStatistics {

    @Query("MATCH (t: Transaction) WHERE $type IN labels(t) RETURN count(*) AS count")
    long countByType(String type);
}
