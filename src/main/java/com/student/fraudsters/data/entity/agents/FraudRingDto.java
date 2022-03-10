package com.student.fraudsters.data.entity.agents;

public class FraudRingDto {

    private final Integer groupId;
    private final Integer groupSize;

    public FraudRingDto(Integer groupId, Integer groupSize) {
        this.groupId = groupId;
        this.groupSize = groupSize;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public Integer getGroupSize() {
        return groupSize;
    }
}
