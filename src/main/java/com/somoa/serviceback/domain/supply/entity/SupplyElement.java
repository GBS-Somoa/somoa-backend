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
        if (name.equals(LIMIT)) {
            if (SupplyType.isLiquidType(type)) return 100; // ml
            if (type.equals(SupplyType.REPLACEABLE_FILTER)) return 365; // 일
            if (type.equals(SupplyType.CLEANABLE_FILTER)) return FilterStatus.BAD;
            if (type.equals(SupplyType.SUPPLY_TANK)) return 2;
            if (type.equals(SupplyType.DRAIN_TANK)) return 8;
            if (type.equals(SupplyType.DUST_BIN)) return 8;
        }
        if (name.equals(AMOUNT)) return 0;
        if (name.equals(AMOUNT_TMP)) return 0;
        if (name.equals(CHANGE_DATE)) return LocalDateTime.now().plusHours(9);
        if (name.equals(STATUS)) return FilterStatus.GOOD;

        throw new IllegalArgumentException("유효하지 않은 소모품 타입입니다 : " + name);
    }

    public static boolean isValidElement(String name) {
        return supplyElementSet.contains(name);
    }
}
