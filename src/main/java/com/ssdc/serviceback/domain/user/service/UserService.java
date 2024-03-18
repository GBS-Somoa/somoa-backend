package com.ssdc.serviceback.domain.user.service;

import com.ssdc.serviceback.global.auth.JwtService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class UserService {

    private final ReactiveUserDetailsService users;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public UserService(ReactiveUserDetailsService users, JwtService jwtService, PasswordEncoder encoder) {
        this.users = users;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    public Mono<Map<String, String>> loginUser(String username, String password) {
        return users.findByUsername(username)
                .filter(userDetails -> encoder.matches(password, userDetails.getPassword()))
                .flatMap(userDetails -> jwtService.generateTokens(userDetails.getUsername())) // 비동기 메소드 호출로 변경
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid username or password")));
    }

    public Mono<Map<String, String>> refreshAccessToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            return Mono.error(new IllegalArgumentException("Invalid or expired refresh token"));
        }

        return Mono.just(refreshToken)
                .flatMap(token -> Mono.justOrEmpty(jwtService.getUserNameFromRefreshToken(token)))
                .flatMap(username -> {
                    if (username == null || username.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Token does not contain a valid username"));
                    }
                    return jwtService.generateTokens(username); // 비동기 메소드 호출로 변경
                });
    }
}