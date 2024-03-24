package com.somoa.serviceback.domain.supply.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SupplyRegisterParam {

    private String type;
    private String name;
    private Map<String, Object> details = new HashMap<>();
}
