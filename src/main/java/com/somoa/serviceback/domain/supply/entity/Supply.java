package com.somoa.serviceback.domain.supply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder
    public Supply(String type, String name, Map<String, Object> details) {
        this.type = type;
        this.name = name;
        this.details = details;
    }
}
