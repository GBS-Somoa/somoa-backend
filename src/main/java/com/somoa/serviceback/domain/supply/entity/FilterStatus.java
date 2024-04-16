package com.somoa.serviceback.domain.supply.entity;

public class FilterStatus {

    public static final String GOOD = "good";
    public static final String NORMAL = "normal";
    public static final String BAD = "bad";
    public static final String NULL = "null";

    // 같은이름의 서로다른형식의 status 관리
    public static int statusToNumber(Object statusValue) {
        if (statusValue instanceof String) {
            switch ((String) statusValue) {
                case "good": return 1;
                case "normal": return 2;
                case "bad": return 3;
                default: return 0; // 알 수 없는 상태
            }
        } else if (statusValue instanceof Integer) {
            return (Integer) statusValue;
        }
        return -1; // 처리할 수 없는 타입
    }
}
