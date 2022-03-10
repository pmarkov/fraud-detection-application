package com.student.fraudsters.controllers;

import com.student.fraudsters.data.entity.transactions.TransactionFrequenciesDto;
import com.student.fraudsters.data.entity.transactions.TransactionMarketStatisticsDto;
import com.student.fraudsters.services.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/statistics")
    public Collection<TransactionMarketStatisticsDto> getTransactionStatistics() {
        return transactionService.getTransactionMarketMetrics();
    }

    @GetMapping("/frequencies")
    public Collection<TransactionFrequenciesDto> getTransactionFrequencies() {
        return transactionService.getTransactionFrequencies();
    }

    @GetMapping("/count")
    public Map<String, Long> getAllTransactionsCount() {
        return transactionService.countAllTransactions();
    }

    @GetMapping("/cash-out/count")
    public Map<String, Long> getCashOutTransactionsCount() {
        return transactionService.countCashOutTransactions();
    }


    @GetMapping("/cash-in/count")
    public Map<String, Long> getCashInTransactionsCount() {
        return transactionService.countCashInTransactions();
    }


    @GetMapping("/debit/count")
    public Map<String, Long> getDebitTransactionsCount() {
        return transactionService.countDebitTransactions();
    }


    @GetMapping("/transfer/count")
    public Map<String, Long> getTransferTransactionsCount() {
        return transactionService.countTransferTransactions();
    }

    @GetMapping("/payment/count")
    public Map<String, Long> getPaymentTransactionsCount() {
        return transactionService.countPaymentTransactions();
    }
}
