package com.student.fraudsters.data.entity.transactions;

public class TransactionFrequenciesDto {

    private final String transactionType;
    private final long absoluteFrequency;
    private final double relativeFrequency;

    public TransactionFrequenciesDto(String transactionType, long absoluteFrequency, double relativeFrequency) {
        this.transactionType = transactionType;
        this.absoluteFrequency = absoluteFrequency;
        this.relativeFrequency = relativeFrequency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public long getAbsoluteFrequency() {
        return absoluteFrequency;
    }

    public double getRelativeFrequency() {
        return relativeFrequency;
    }
}
