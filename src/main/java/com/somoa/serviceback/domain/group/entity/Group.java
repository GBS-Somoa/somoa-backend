package com.somoa.serviceback.domain.group.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("`group`")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @Column("group_id")
    private Integer id;

    @Column("group_name")
    private String name;
}
