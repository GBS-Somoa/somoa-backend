package com.somoa.serviceback.domain.supply.entity;

import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;

public class SupplyElement {

    public static final String LIMIT = "supplyLimit";
    public static final String CHANGE_DATE = "supplyChangeDate";
    public static final String STATUS = "supplyStatus";
    public static final String AMOUNT = "supplyAmount";
    public static final String AMOUNT_TMP = "supplyAmountTmp";

    public static final HashSet<String> supplyElementSet = new HashSet<String>() {{
        add(LIMIT);
        add(CHANGE_DATE);
        add(STATUS);
        add(AMOUNT);
        add(AMOUNT_TMP);
    }};

    public static Object getDefaultValue(String name, String type) {
        switch (name) {
            case LIMIT:
                return SupplyType.getDefaultLimit(type);
            case AMOUNT:
            case AMOUNT_TMP:
                return 0;
            case CHANGE_DATE:
                return LocalDateTime.now().plusHours(9);
            case STATUS:
                return FilterStatus.GOOD;
        }

        throw new IllegalArgumentException("유효하지 않은 소모품 타입입니다 : " + name);
    }

    public static boolean isValidElement(String name) {
        return supplyElementSet.contains(name);
    }
}
