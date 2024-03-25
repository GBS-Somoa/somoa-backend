package com.somoa.serviceback.domain.supply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SupplyRegisterParam {

    @JsonProperty("supplyType")
    private String type;

    @JsonProperty("supplyName")
    private String name;

    @JsonProperty("dataProvided")
    private Map<String, Object> details = new HashMap<>();
}
