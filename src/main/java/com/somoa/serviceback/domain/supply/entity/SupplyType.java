package com.somoa.serviceback.domain.supply.entity;

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

    private static final String[] supplyTypes = {
            REPLACEABLE_FILTER, CLEANABLE_FILTER, SUPPLY_TANK, DRAIN_TANK, DUST_BIN,
            WASHER_DETERGENT, DISH_DETERGENT, FABRIC_SOFTENER, DISH_RINSE
    };

    private static final String[] supplyLiquidTypes = {
            WASHER_DETERGENT, DISH_DETERGENT, FABRIC_SOFTENER, DISH_RINSE
    };

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
}
