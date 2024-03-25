package com.somoa.serviceback.domain.device.dto;

import com.somoa.serviceback.domain.device.entity.Device;
import com.somoa.serviceback.domain.device.entity.DeviceType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DeviceResponse {

    private String id;
    private String nickname;
    private String model;
    private String type;
    private String manufacturer;

    public static DeviceResponse of(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .nickname(device.getNickname())
                .model(device.getModel())
                .type(device.getType())
                .manufacturer(device.getManufacturer())
                .build();
    }
}
