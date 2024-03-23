package com.somoa.serviceback.domain.device.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeviceType {

    WASHER("washer"),
    REFRIGERATOR("refrigerator"),
    STREAM_CLOSET("streamCloset"),
    DISHWASHER("dishwasher"),
    WATER_PURIFIER("waterPurifier"),
    DEHUMIDIFIER("dehumidifier"),
    AIR_PURIFIER("airPurifier"),
    VACUUM_CLEANER("vacuumCleaner"),
    AIR_CONDITIONER("airConditioner"),
    HUMIDIFIER("humidifier"),
    ;

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static DeviceType fromValue(String value) {
        for (DeviceType type : DeviceType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported Device Type : " + value);
    }
}
