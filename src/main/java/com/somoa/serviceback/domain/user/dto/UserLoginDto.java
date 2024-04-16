package com.somoa.serviceback.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginDto {
    private String username;
    private String password;
    private String mobileDeviceId;
    private String fcmToken;
}
