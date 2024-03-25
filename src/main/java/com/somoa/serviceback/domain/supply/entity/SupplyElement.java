package com.somoa.serviceback.domain.supply.entity;

import java.util.HashSet;

public class SupplyElement {

    public static final String LIMIT = "supplyLimit";
    public static final String CHANGE_DATE = "supplyChangeDate";
    public static final String STATUS = "supplyStatus";
    public static final String AMOUNT = "supplyAmount";
    public static final String AMOUNT_TMP = "supplyAmountTmp";

    public static boolean isValidType(String name, String value) {
        if (name.equals(LIMIT)) return true;
        if (name.equals(CHANGE_DATE)) return true;
        if (name.equals(STATUS))  return true;
        return false;
    }

    public static boolean isValidType(String name, Integer value) {
        if (name.equals(LIMIT)) return true;
        if (name.equals(AMOUNT)) return true;
        if (name.equals(AMOUNT_TMP)) return true;
        return false;
    }
}
