package com.somoa.serviceback.domain.supply.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.somoa.serviceback.domain.supply.entity.Supply;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplyResponse {

    private String id;
    private String type;
    private String name;
    private Map<String, Object> details;
    private Map<String,Object> limit;
    private Integer supplyAmountTmp;

    public static SupplyResponse of(Supply supply) {
        SupplyResponseBuilder builder = SupplyResponse.builder()
                .id(supply.getId())
                .type(supply.getType())
                .name(supply.getName())
                .details(supply.getDetails())
                .limit(supply.getSupplyLimit());
        System.out.println(supply.getSupplyAmountTmp());
        if(supply.getSupplyAmountTmp() != null&&!supply.getSupplyAmountTmp().equals("null")) {
            System.out.println(supply);
            builder.supplyAmountTmp(supply.getSupplyAmountTmp());
        }
        return builder.build();
    }
}
