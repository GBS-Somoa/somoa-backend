package com.somoa.serviceback.domain.supply.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "supply")
public class Supply {

    @MongoId
    private String id;

    @Field("supplyType")
    private String type;

    @Field("supplyName")
    private String name;

    @Field("supplyDetails")
    private Map<String, Object> details = new HashMap<>();

    @Field("supplyLimit")
    private Map<String, Object> supplyLimit = new HashMap<>();

    @Builder
    public Supply(String type, String name, Map<String, Object> details, Map<String, Object> supplyLimit) {
        this.type = type;
        this.name = name;
        this.details = details;
        this.supplyLimit = supplyLimit;
    }

    @Field("supplyAmountTmp")
    private Integer amountTmp;
}
