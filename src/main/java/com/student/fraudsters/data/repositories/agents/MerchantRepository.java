package com.student.fraudsters.data.repositories.agents;

import com.student.fraudsters.data.entity.agents.Merchant;
import org.springframework.data.repository.Repository;

public interface MerchantRepository extends Repository<Merchant, String> {
    Merchant findMerchantById(String id);
    long count();
}
