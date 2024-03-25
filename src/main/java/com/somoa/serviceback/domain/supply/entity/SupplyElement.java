package com.somoa.serviceback.domain.supply.entity;

import java.util.HashSet;

public class SupplyElement {

    public static final String LIMIT = "limit";
    public static final String CHANGE_DATE = "changeDate";
    public static final String STATUS = "status";
    public static final String AMOUNT = "amount";
    public static final String AMOUNT_TMP = "amountTmp";

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
