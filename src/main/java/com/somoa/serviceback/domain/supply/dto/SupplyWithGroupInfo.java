package com.somoa.serviceback.domain.supply.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SupplyWithGroupInfo {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> details;
    private Map<String, Object> supplyLimit;
    private Integer supplyAmountTmp;
    private Integer groupId;
    private String groupName;

    public void setCareNeeded(boolean conditionMet) {
    }
}
