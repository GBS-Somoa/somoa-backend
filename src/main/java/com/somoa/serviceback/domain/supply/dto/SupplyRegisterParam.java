package com.somoa.serviceback.domain.supply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class SupplyRegisterParam {

    @JsonProperty("supplyType")
    private String type;

    @JsonProperty("supplyName")
    private String name;

    @JsonProperty("dataProvided")
    private Set<String> details = new HashSet<>();
}
