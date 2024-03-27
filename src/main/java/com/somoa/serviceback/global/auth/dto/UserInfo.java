package com.somoa.serviceback.global.auth.dto;

import com.somoa.serviceback.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {

    private Integer id;
    private String username;

    public static UserInfo of(User user) {
        return new UserInfo(user.getId(), user.getUsername());
    }
}
