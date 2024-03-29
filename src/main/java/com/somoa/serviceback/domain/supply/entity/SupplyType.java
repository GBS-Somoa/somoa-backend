package com.somoa.serviceback.domain.supply.entity;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplyType {

    public static final String REPLACEABLE_FILTER = "replaceableFilter";
    public static final String CLEANABLE_FILTER = "cleanableFilter";
    public static final String SUPPLY_TANK = "supplyTank";
    public static final String DRAIN_TANK = "drainTank";
    public static final String DUST_BIN = "dustBin";
    public static final String WASHER_DETERGENT = "washerDetergent";
    public static final String DISH_DETERGENT = "dishDetergent";
    public static final String FABRIC_SOFTENER = "fabricSoftener";
    public static final String DISH_RINSE = "dishRinse";

    /**
     *  필터류의 status는  별도로 지정해야함.
     */
    private static final Map<String, Object> defaultLimits = new HashMap<>() {{
        put(REPLACEABLE_FILTER, 365);
        put(CLEANABLE_FILTER, 365);
        put(SUPPLY_TANK, 10);
        put(DRAIN_TANK, 90);
        put(DUST_BIN, 8);
        put(WASHER_DETERGENT, 100);
        put(DISH_DETERGENT, 100);
        put(FABRIC_SOFTENER, 100);
        put(DISH_RINSE, 100);
    }};

    private static final Map<String, Object> defaultDetails = new HashMap<>() {{
        put(REPLACEABLE_FILTER, Instant.now());
        put(CLEANABLE_FILTER, FilterStatus.GOOD);
        put(SUPPLY_TANK, 0);
        put(DRAIN_TANK, 0);
        put(DUST_BIN, 10);
        put(WASHER_DETERGENT, 0);
        put(DISH_DETERGENT, 0);
        put(FABRIC_SOFTENER, 0);
        put(DISH_RINSE, 0);
    }};

    private static final String[] Action = {
            "change", "clean", "add"
    };

    // 나머지 클래스 내용...

    public static String[] getActions() {
        return Action.clone(); // 배열의 복사본을 반환
    }

    private static final List<String> filterTypes = Arrays.asList(REPLACEABLE_FILTER, CLEANABLE_FILTER);


    private static final String[] supplyTypes = {
            REPLACEABLE_FILTER, CLEANABLE_FILTER, SUPPLY_TANK, DRAIN_TANK, DUST_BIN,
            WASHER_DETERGENT, DISH_DETERGENT, FABRIC_SOFTENER, DISH_RINSE
    };

    private static final String[] supplyLiquidTypes = {
            WASHER_DETERGENT, DISH_DETERGENT, FABRIC_SOFTENER, DISH_RINSE
    };

    public static Object getDefaultLimit(String type) {
        return defaultLimits.get(type);
    }
    public static Object getDefaultDetail(String type) {
        return defaultDetails.get(type);
    }

    public static boolean isLiquidType(String type) {
        for (String supplyLiquidType : supplyLiquidTypes) {
            if (type.equals(supplyLiquidType))
                return true;
        }
        return false;
    }

    public static boolean isValidType(String type) {
        for (String supplyType : supplyTypes) {
            if (type.equals(supplyType))
                return true;
        }
        return false;
    }
    
    public static String getKoreanForType(String type){
        switch (type) {
            case REPLACEABLE_FILTER:
                return "교체";
            case CLEANABLE_FILTER:
            case DRAIN_TANK:
            case DUST_BIN:
                return "청소";
            case SUPPLY_TANK:
            case WASHER_DETERGENT:
            case DISH_DETERGENT:
            case FABRIC_SOFTENER:
            case DISH_RINSE:
                return "추가";
            default:
                return "null";
        }
    }


    public static String getActionForType(String type) {
        switch (type) {
            case REPLACEABLE_FILTER:
                return Action[0];
            case CLEANABLE_FILTER:
            case DRAIN_TANK:
            case DUST_BIN:
                return Action[1];
            case SUPPLY_TANK:
            case WASHER_DETERGENT:
            case DISH_DETERGENT:
            case FABRIC_SOFTENER:
            case DISH_RINSE:
                return Action[2];
            default:
                return "null";
        }
    }

    public static boolean validateSupplyLimitKey(String type,String key, Object value) {
        switch (key) {
            case "supplyAmount":
            case "supplyChangeDate":
            case "supplyLevel":
                if (!(value instanceof Integer) || (Integer) value < 0) {
                    System.out.println(key + "는 정수이며 0 이상이어야 합니다.");
                    return false;
                }
                break;
            case "supplyStatus":
                if(filterTypes.contains(type)) {
                    if(!isValidFilterSupplyStatus(value)) {
                        System.out.println("필터류의 supplyStatus 값이 아닙니다.");
                        return false;
                    }
                }
                else if (!isValidSupplyStatus(value)) {
                    System.out.println("유효하지 않은 supplyStatus 값입니다.");
                    return false;
                }
                break;
            default:
                System.out.println("알 수 없는 키입니다: " + key);
                return false;
        }
        return true;
    }

    private static boolean isValidFilterSupplyStatus(Object value){
        return Arrays.asList("good", "normal", "bad","null").contains(value);
    }
    private static boolean isValidSupplyStatus(Object value) {
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            return intValue >= 1 && intValue <= 10;
        }
        return false;
    }
}
