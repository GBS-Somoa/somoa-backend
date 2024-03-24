package com.somoa.serviceback.domain.devicesupplies.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("device_supplies")
@Data
public class DeviceSupplies {

    @Id
    @Column("device_supplies_id")
    private Integer deviceSuppliesId;

    @Column("device_id")
    private String deviceId;

    @Column("supplies_id")
    private String suppliesId;

    @Column("created_at")
    private Timestamp createdAt;

    @Column("updated_at")
    private Timestamp updatedAt;
}
