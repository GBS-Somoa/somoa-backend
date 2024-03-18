package com.ssdc.serviceback.global.auth;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReqLogin {
    private String username;
    private String password;
}
