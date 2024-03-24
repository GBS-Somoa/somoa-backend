package com.somoa.serviceback.domain.supply.dto;

import com.somoa.serviceback.domain.supply.entity.Supply;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SupplyRegisterParam {

    private String type;
    private String name;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Supply toEntity() {
        return Supply.builder()
                .type(type)
                .name(name)
                .details(additionalProperties)
                .build();
    }
}
