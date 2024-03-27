package com.somoa.serviceback.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.somoa.serviceback.domain.supply.dto.SupplyStatusParam;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceApiStatusResponse {

    @JsonProperty("deviceId")
    private String id;

    @JsonProperty("deviceModel")
    private String model;

    @JsonProperty("deviceType")
    private String type;

    @JsonProperty("deviceManufacturer")
    private String manufacturer;

    private List<SupplyStatusParam> supplies;
}
