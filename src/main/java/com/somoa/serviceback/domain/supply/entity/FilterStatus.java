package com.somoa.serviceback.domain.supply.entity;

public class FilterStatus {

    public static final String GOOD = "good";
    public static final String NORMAL = "normal";
    public static final String BAD = "bad";
    public static final String NULL = "null";

    /**
     * 상태를 숫자로 변환합니다.
     * @param status 상태 문자열 ("good", "normal", "bad", "null")
     * @return 상태에 해당하는 숫자
     */
    public static int statusToNumber(String status) {
        switch (status) {
            case GOOD:
                return 3;
            case NORMAL:
                return 2;
            case BAD:
                return 1;
            case NULL:
            default:
                return 0;
        }
    }
}
