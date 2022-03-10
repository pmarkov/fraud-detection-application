package com.student.fraudsters.data.repositories.agents;

import com.student.fraudsters.data.entity.agents.Bank;
import org.springframework.data.repository.Repository;


public interface BankRepository extends Repository<Bank, String> {
    Bank findBankById(String id);
    long count();
}
