package com.somoa.serviceback.domain.device.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceResponse {

    private String model;
    private String type;
    private String manufacturer;
}
