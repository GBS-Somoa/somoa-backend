package com.somoa.serviceback.domain.supply.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SupplyWithGroupInfo {
    private String supplyId;
    private String deviceId;
    private String deviceNickname;
    private Integer groupId;
    private String groupName;
}
