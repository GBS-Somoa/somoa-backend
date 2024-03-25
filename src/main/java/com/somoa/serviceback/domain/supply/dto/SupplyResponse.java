package com.somoa.serviceback.domain.supply.dto;

import com.somoa.serviceback.domain.supply.entity.Supply;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

@Data
@Builder
public class SupplyResponse {

    private String id;
    private String type;
    private String name;
    private Map<String, Object> details;
    private Map<String,Object> Limit;

    public static SupplyResponse of(Supply supply) {
        return SupplyResponse.builder()
                .id(supply.getId())
                .type(supply.getType())
                .name(supply.getName())
                .details(supply.getDetails())
                .Limit(supply.getSupplyLimit())
                .build();
    }
}
