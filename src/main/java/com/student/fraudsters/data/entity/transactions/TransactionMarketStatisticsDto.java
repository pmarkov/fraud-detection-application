package com.student.fraudsters.data.entity.transactions;

public class TransactionMarketStatisticsDto {

    private final String transactionType;
    private final double totalMarketValue;
    private final double marketValuePercent;
    private final double marketTransactionsPercent;
    private final long avgTransactionValue;
    private final long numberOfTransactions;

    public TransactionMarketStatisticsDto(String transactionType, double totalMarketValue,
                                          double marketValuePercent, double marketTransactionsPercent,
                                          long avgTransactionValue, long numberOfTransactions) {
        this.transactionType = transactionType;
        this.totalMarketValue = totalMarketValue;
        this.marketValuePercent = marketValuePercent;
        this.marketTransactionsPercent = marketTransactionsPercent;
        this.avgTransactionValue = avgTransactionValue;
        this.numberOfTransactions = numberOfTransactions;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    public double getMarketValuePercent() {
        return marketValuePercent;
    }

    public double getMarketTransactionsPercent() {
        return marketTransactionsPercent;
    }

    public long getAvgTransactionValue() {
        return avgTransactionValue;
    }

    public long getNumberOfTransactions() {
        return numberOfTransactions;
    }
}
