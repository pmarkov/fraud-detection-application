package com.student.fraudsters.data.repositories.agents;

import com.student.fraudsters.data.entity.agents.FraudRingDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FraudRings {

    @Transactional(readOnly = true)
    List<FraudRingDto> getFraudRingsOrderedBySize(int minimumSize);
}
