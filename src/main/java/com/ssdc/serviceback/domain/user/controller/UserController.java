package com.ssdc.serviceback.domain.user.controller;

import com.ssdc.serviceback.global.auth.JwtService;
import com.ssdc.serviceback.global.auth.ReqLogin;
import com.ssdc.serviceback.global.auth.ReqRespModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    final ReactiveUserDetailsService users;
    final JwtService jwtService;

    final PasswordEncoder encoder;


    public UserController(ReactiveUserDetailsService users, JwtService jwtService, PasswordEncoder encoder) {
        this.users = users;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    @GetMapping("/auth")
    public Mono<ResponseEntity<ReqRespModel<String>>> auth(){
        return Mono.just(
                ResponseEntity.ok(
                        new ReqRespModel<>("하이","")
                )
        );
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<?>> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        String refreshToken = tokenRefreshRequest.getRefreshToken();
        if (jwtService.validateRefreshToken(refreshToken)) {
            String username = jwtService.getUserNameFromRefreshToken(refreshToken);
            if (username != null) {
                var tokens = jwtService.generateTokens(username);
                return Mono.just(ResponseEntity.ok().body(tokens));
            }
        }
        return Mono.just(ResponseEntity.status(401).body("Invalid or expired refresh token"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<Map<String, String>>>> login(@RequestBody ReqLogin user){
        return users.findByUsername(user.getUsername())
                .filter(u -> encoder.matches(user.getPassword(), u.getPassword()))
                .map(u -> {
                    Map<String, String> tokens = jwtService.generateTokens(u.getUsername());
                    return ResponseEntity.ok(new ReqRespModel<>(tokens, "성공"));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqRespModel<>(null, "유효하지 않은 사용자 이름 또는 비밀번호")));
    }
    @GetMapping("/check")
    public Mono<ResponseEntity<String>> checkBlocking() {
        try {
            // 블로킹 작업 시뮬레이션
            Thread.sleep(1000); // 1초 동안 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error"));
        }
        return Mono.just(ResponseEntity.ok("hello"));
    }


    static class TokenRefreshRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

}
