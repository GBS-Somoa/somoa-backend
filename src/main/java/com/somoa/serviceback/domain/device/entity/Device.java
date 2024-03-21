package com.somoa.serviceback.domain.device.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @Column("device_id")
    private Integer id;

    @Column("device_code")
    private String code;

    @Column("device_manufacturer")
    private String manufacturer;

    @Column("device_type")
    private String type;

    @Column("device_model")
    private String model;

    @Column("device_nickname")
    private String nickname;

    @Column("group_id")
    private Integer groupId;
}
