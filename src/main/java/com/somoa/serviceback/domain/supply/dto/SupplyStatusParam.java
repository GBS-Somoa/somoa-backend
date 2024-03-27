package com.somoa.serviceback.domain.supply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class SupplyStatusParam {

    @JsonProperty("supplyType")
    private String type;

    @JsonProperty("supplyName")
    private String name;

    @JsonProperty("dataProvided")
    private List<String> details = new ArrayList<>();

    @JsonProperty("values")
    private List<String> values = new ArrayList<>();
}
