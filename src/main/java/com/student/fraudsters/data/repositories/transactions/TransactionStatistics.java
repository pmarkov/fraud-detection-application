package com.student.fraudsters.data.repositories.transactions;

import com.student.fraudsters.data.entity.transactions.TransactionMarketStatisticsDto;
import com.student.fraudsters.data.entity.transactions.TransactionFrequenciesDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface TransactionStatistics {

    @Transactional(readOnly = true)
    Collection<TransactionMarketStatisticsDto> getTransactionMarketMetrics();

    @Transactional(readOnly = true)
    Collection<TransactionFrequenciesDto> getTransactionFrequencies();
}
