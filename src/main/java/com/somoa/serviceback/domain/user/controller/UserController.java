package com.somoa.serviceback.domain.user.controller;

import com.somoa.serviceback.domain.user.dto.UserLoginDto;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.domain.user.service.UserService;
import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import com.somoa.serviceback.global.fcm.service.FcmService;
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

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    final ReactiveUserDetailsService users;
    final JwtService jwtService;
    final AuthConverter authConverter;
    final FcmService fcmService;

    private final FcmRepository fcmRepository;

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
    public Mono<ResponseEntity<ResponseHandler>> login(@RequestBody UserLoginDto user) {
        return userService.loginUser(user.getUsername(), user.getPassword())
                .flatMap(response -> {
                    String userId = response.get("userId").toString();
                    Map<String, String> tokens = (Map<String, String>) response.get("tokens");
                    // FCM 토큰 저장 로직 호출
                    return fcmService.saveOrUpdateFcmToken(Integer.parseInt(userId), user.getMobileDeviceId(), user.getFcmToken())
                            .then(ResponseHandler.ok(tokens, "로그인 성공")); // 변경된 부분
                })
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

    /**
     * 모든 사용자에게 알림을 보내는 엔드포인트(참고코드용)
     * @return
     */
    @GetMapping("/sendNotifications")
    public Mono<ResponseEntity<String>> sendNotificationsToAllUsers() {
        // FcmToken 컬렉션에서 모든 토큰 조회
        return fcmRepository.findAll()
                .flatMap(fcmToken -> {
                    // 각 토큰에 대해 메시지 전송
                    FcmSendDto fcmSendDto = new FcmSendDto(fcmToken.getToken(), "제목", "본문");
                    try {
                        return Mono.just(fcmService.sendMessageTo(fcmSendDto));
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                })
                .collectList() // 모든 메시지 전송 작업을 리스트로 수집
                .map(resultList -> ResponseEntity.ok().body("Messages sent successfully"))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tokens found"));
    }
}
