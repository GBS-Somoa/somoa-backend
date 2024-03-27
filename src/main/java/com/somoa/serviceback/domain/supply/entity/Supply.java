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
@Builder
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

    // @Builder.Default 어노테이션을 사용하여 기본값 설정
    @Field("supplyAmountTmp")
    private Integer amountTmp; // 기본값은 0
}