package com.somoa.serviceback.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.somoa.serviceback.domain.device.entity.DeviceType;
import com.somoa.serviceback.domain.supply.dto.SupplyRegisterParam;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceExternalApiResponse {

    @JsonProperty("deviceModel")
    private String model;

    @JsonProperty("deviceType")
    private String type;

    @JsonProperty("deviceManufacturer")
    private String manufacturer;

    private List<SupplyRegisterParam> supplies;
}
