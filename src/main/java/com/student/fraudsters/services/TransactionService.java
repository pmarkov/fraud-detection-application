package com.student.fraudsters.services;

import com.student.fraudsters.data.entity.transactions.TransactionFrequenciesDto;
import com.student.fraudsters.data.entity.transactions.TransactionMarketStatisticsDto;
import com.student.fraudsters.data.repositories.transactions.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Long> countAllTransactions() {
        return Map.of("result", transactionRepository.countByType("Transaction"));
    }

    public Map<String, Long> countCashInTransactions() {
        return Map.of("result", transactionRepository.countByType("CashIn"));
    }

    public Map<String, Long> countCashOutTransactions() {
        return Map.of("result", transactionRepository.countByType("CashOut"));
    }

    public Map<String, Long> countDebitTransactions() {
        return Map.of("result", transactionRepository.countByType("Debit"));
    }

    public Map<String, Long> countPaymentTransactions() {
        return Map.of("result", transactionRepository.countByType("Payment"));
    }

    public Map<String, Long> countTransferTransactions() {
        return Map.of("result", transactionRepository.countByType("Transfer"));
    }

    public Collection<TransactionFrequenciesDto> getTransactionFrequencies() {
        return transactionRepository.getTransactionFrequencies();
    }

    public Collection<TransactionMarketStatisticsDto> getTransactionMarketMetrics() {
        return transactionRepository.getTransactionMarketMetrics();
    }

}
