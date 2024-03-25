package com.somoa.serviceback.domain.device.dto;

import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.entity.DeviceType;
import com.somoa.serviceback.domain.supply.dto.SupplyResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DeviceResponse {

    private String id;
    private String nickname;
    private String model;
    private String type;
    private String manufacturer;
    private List<SupplyResponse> supplies;

    public static DeviceResponse of(Device device, List<SupplyResponse> supplies) {
        return DeviceResponse.builder()
                .id(device.getId())
                .nickname(device.getNickname())
                .model(device.getModel())
                .type(device.getType())
                .manufacturer(device.getManufacturer())
                .supplies(supplies)
                .build();
    }
}
