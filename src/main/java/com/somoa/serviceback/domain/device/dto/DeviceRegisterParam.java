package com.somoa.serviceback.domain.device.dto;

import com.somoa.serviceback.domain.supply.dto.SupplyRegisterParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceRegisterParam {

    private Integer groupId;
    private String code;
    private String nickname;
}
