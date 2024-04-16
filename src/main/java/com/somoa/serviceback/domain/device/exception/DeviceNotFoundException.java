package com.somoa.serviceback.domain.device.exception;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(String message) {
        super(message);
    }
}
