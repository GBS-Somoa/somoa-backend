package com.somoa.serviceback.domain.groupuser.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("group_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUser {

    @Id
    @Column("group_user_id")
    private Integer id;

    @Column("user_id")
    private Integer userId;

    @Column("group_id")
    private Integer groupId;

    @Column("ordered_num")
    private int orderedNum;

    @Column("role")
    private String role;
}
