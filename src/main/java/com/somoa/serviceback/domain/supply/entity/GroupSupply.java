package com.somoa.serviceback.domain.supply.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("group_supply")
@Data
public class GroupSupply {

    @Id
    @Column("group_supply_id")
    private Integer id;

    @Column("group_id")
    private Integer groupId;

    @Column("supply_id")
    private String supplyId;

    @Builder
    public GroupSupply(Integer groupId, String supplyId) {
        this.groupId = groupId;
        this.supplyId = supplyId;
    }
}
