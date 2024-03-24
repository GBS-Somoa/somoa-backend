package com.somoa.serviceback.domain.supplies.dto;

import lombok.Data;

import java.util.List;

@Data
public class SuppliesStatusDto {
    private String supplyType;
    private String suuplyName;
    // supplyStatus, supplyChangeDate || supplyAmount
    private List<String> dataProvided;
    private List<Object> values;
}
