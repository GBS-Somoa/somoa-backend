package com.somoa.serviceback.domain.supply.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SupplyLimitParam {
    private Map<String,Object> supplyLimit;
}
