package com.somoa.serviceback.domain.user.controller;

import com.somoa.serviceback.domain.user.dto.UserLoginDto;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.domain.user.service.UserService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import com.somoa.serviceback.global.auth.AuthConverter;
import com.somoa.serviceback.global.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    final ReactiveUserDetailsService users;
    final JwtService jwtService;
    final AuthConverter authConverter;



    @GetMapping("/refresh")
    public Mono<ResponseEntity<ResponseHandler>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            return userService.refreshAccessToken(refreshToken)
                    .flatMap(tokens -> ResponseHandler.ok(tokens,"토큰이 정상적으로 갱신되었습니다."))
                    .onErrorResume(e -> {
                        System.out.println(e);
                        return ResponseHandler.error("유효하지 않거나 기간이 지난 토큰입니다.",HttpStatus.UNAUTHORIZED); // 401 Unauthorized
                    });
        } else {
            return ResponseHandler.error("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }



    @PostMapping("/login")
    public Mono<ResponseEntity<ResponseHandler>> login(@RequestHeader("Authorization") String FcmToken,@RequestBody UserLoginDto user) {
        System.out.println(FcmToken);
        return userService.loginUser(user.getUsername(), user.getPassword())
                .flatMap(tokens -> ResponseHandler.ok(tokens, "로그인 성공"))
                .onErrorResume(e -> {
                    if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                        // 인증 실패(사용자 이름 또는 비밀번호 오류)의 경우
                        return ResponseHandler.error("유효하지 않은 사용자 이름 또는 비밀번호", HttpStatus.UNAUTHORIZED);
                    } else {
                        // 그 외 실패(예: 토큰 생성 실패)의 경우
                        return ResponseHandler.error("서버 오류로 인해 로그인을 처리할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }


    @GetMapping("/auth")
    public Mono<ResponseEntity<ResponseHandler>> auth() {
        return ResponseHandler.ok("인증성공", "인증이 성공적으로 완료되었습니다.");
    }


    @PostMapping("/signup")
    public Mono<ResponseEntity<ResponseHandler>> signUp(@RequestBody UserSignupDto userSignupDto) {
        return userService.signUp(userSignupDto)
                .flatMap(data -> ResponseHandler.ok(data, "회원가입이 성공적으로 완료되었습니다."))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error(error.getMessage(), HttpStatus.BAD_REQUEST);
                    }
                    return ResponseHandler.error("내부 서버 오류로 인해 처리할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping("/blockcheck")
    public Mono<ResponseEntity<ResponseHandler>> checkBlocking() {
        try {
            // 블로킹 작업 시뮬레이션
            Thread.sleep(1000); // 1초 동안 대기
            return ResponseHandler.ok("hello", "처리 성공");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseHandler.error("에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check")
    public Mono<ResponseEntity<ResponseHandler>> checkAuth() {
        System.out.println("인증완료");
            return ResponseHandler.ok("hello", "인증된 유저");

    }
}
