package com.somoa.serviceback.domain.device.dto;

import lombok.Data;

@Data
public class ExternalApiResponse {

    private String message;
    private DeviceApiResponse data;
}
