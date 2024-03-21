package com.somoa.serviceback.domain.device.dto;

import lombok.Data;

@Data
public class DeviceRegisterParam {

    private Integer groupId;
    private String code;
    private String nickname;
}
