package com.somoa.serviceback.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("user")
@Getter
@Data
public class User {

    @Id
    @Column("user_id")
    private int id;

    @Column("user_username")
    private String username;

    @Column("user_password")
    private String password;

    @Column("user_nickname")
    private String nickname;

    @Column("created_at")
    private Timestamp createdAt;

    @Column("updated_at")
    private Timestamp updatedAt;
}
