package com.somoa.serviceback.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.somoa.serviceback.domain.supply.dto.SupplyRegisterParam;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class DeviceApiResponse {

    @JsonProperty("deviceModel")
    private String model;

    @JsonProperty("deviceType")
    private String type;

    @JsonProperty("deviceManufacturer")
    private String manufacturer;

    private List<SupplyRegisterParam> supplies;
}
