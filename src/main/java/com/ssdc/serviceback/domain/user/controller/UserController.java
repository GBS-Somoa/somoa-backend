package com.ssdc.serviceback.domain.user.controller;

import com.ssdc.serviceback.domain.user.dto.UserLoginDto;
import com.ssdc.serviceback.domain.user.dto.UserSignUpDto;
import com.ssdc.serviceback.domain.user.service.UserService;
import com.ssdc.serviceback.global.auth.JwtService;
import com.ssdc.serviceback.global.auth.ReqRespModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    final ReactiveUserDetailsService users;
    final JwtService jwtService;


    @GetMapping("/auth")
    public Mono<ResponseEntity<ReqRespModel<String>>> auth(){
        return Mono.just(
                ResponseEntity.ok(
                        new ReqRespModel<>("인증성공","")
                )
        );
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<ReqRespModel<Map<String, String>>>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            return userService.refreshAccessToken(refreshToken)
                    .map(tokens -> ResponseEntity.ok(new ReqRespModel<>(tokens, "토큰이 정상적으로 갱신되었습니다.")))
                    .onErrorResume(e -> {
                        System.out.println(e);
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ReqRespModel<>(null, "유효하지 않거나 기간이 지난 토큰입니다.")));});
        } else {
            return Mono.just(ResponseEntity.badRequest().body(new ReqRespModel<>(null, "토큰이 유효하지 않습니다.")));
        }
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<Map<String, String>>>> login(@RequestBody UserLoginDto user) {
        // 사용자 인증 및 토큰 생성을 직접 처리
        return userService.loginUser(user.getUsername(), user.getPassword())
                .map(tokens -> ResponseEntity.ok(new ReqRespModel<>(tokens, "로그인 성공")))
                .onErrorResume(e -> {
                    if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                        // 인증 실패(사용자 이름 또는 비밀번호 오류)의 경우
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ReqRespModel<>(null, "유효하지 않은 사용자 이름 또는 비밀번호")));
                    } else {
                        // 그 외 실패(예: 토큰 생성 실패)의 경우
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ReqRespModel<>(null, "서버 오류로 인해 로그인을 처리할 수 없습니다.")));
                    }
                });
    }


    @GetMapping("/check")
    public Mono<ResponseEntity<String>> checkBlocking() {
        try {
            // 블로킹 작업 시뮬레이션
            Thread.sleep(1000); // 1초 동안 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러"));
        }
        return Mono.just(ResponseEntity.ok("hello"));
    }
    @PostMapping("/signup")
    public Mono<ResponseEntity<ReqRespModel<String>>> signUp(@RequestBody UserSignUpDto signUpDto) {
        return userService.signUp(signUpDto)
                .map(user -> ResponseEntity.ok(new ReqRespModel<>("회원가입 성공", "")))
                .defaultIfEmpty(ResponseEntity.badRequest().body(new ReqRespModel<>(null, "회원가입 실패")));
    }

}
