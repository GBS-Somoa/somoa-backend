package com.somoa.serviceback.domain.device.dto;

import com.somoa.serviceback.domain.supplies.dto.SuppliesStatusDto;
import lombok.Data;

import java.util.List;

@Data
public class DeviceStatusDto {
       private String deviceModel;
       private String deviceManufacturer;
       private String deviceType;
       private String deviceId;
       //
       private List<SuppliesStatusDto> supplies;

}

