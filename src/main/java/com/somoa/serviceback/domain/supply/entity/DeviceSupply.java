package com.somoa.serviceback.domain.supply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("device_supply")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSupply {

    @Id
    @Column("device_supply_id")
    private Integer id;

    @Column("device_id")
    private String deviceId;

    @Column("supply_id")
    private String supplyId;

    @Builder
    public DeviceSupply(String deviceId, String supplyId) {
        this.deviceId = deviceId;
        this.supplyId = supplyId;
    }
}
