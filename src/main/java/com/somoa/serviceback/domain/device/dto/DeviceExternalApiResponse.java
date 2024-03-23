package com.somoa.serviceback.domain.device.dto;

import com.somoa.serviceback.domain.device.entity.DeviceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceExternalApiResponse {

    private String model;
    private DeviceType type;
    private String manufacturer;
}
